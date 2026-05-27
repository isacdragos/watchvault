import assert from "node:assert/strict";
import http from "node:http";
import { createApp } from "../../server/app.js";

async function createTestContext() {
  const { handler, store } = createApp();
  const server = http.createServer(handler);

  await new Promise((resolve) => {
    server.listen(0, resolve);
  });

  const address = server.address();
  const baseUrl = `http://127.0.0.1:${address.port}`;

  return {
    baseUrl,
    store,
    async request(path, options = {}) {
      return fetch(`${baseUrl}${path}`, {
        ...options,
        headers: {
          "Content-Type": "application/json",
          ...(options.headers ?? {}),
        },
      });
    },
    async close() {
      await new Promise((resolve, reject) => {
        server.close((error) => {
          if (error) {
            reject(error);
            return;
          }

          resolve();
        });
      });
    },
  };
}

const sampleShow = {
  title: "Arcane",
  type: "series",
  status: "watching",
  description: "Animated fantasy drama.",
  episodesWatched: 3,
  totalEpisodes: 9,
  rating: 9.5,
  genres: ["Animation", "Fantasy"],
};

async function runTest(name, callback) {
  try {
    await callback();
    console.log(`PASS ${name}`);
  } catch (error) {
    console.error(`FAIL ${name}`);
    throw error;
  }
}

await runTest("health endpoint reports server status", async () => {
  const context = await createTestContext();

  try {
    const response = await context.request("/api/health", { method: "GET" });
    const body = await response.json();

    assert.equal(response.status, 200);
    assert.deepEqual(body, { status: "ok" });
  } finally {
    await context.close();
  }
});

await runTest("create and fetch a show", async () => {
  const context = await createTestContext();

  try {
    const createResponse = await context.request("/api/shows", {
      method: "POST",
      body: JSON.stringify(sampleShow),
    });
    const createdShow = await createResponse.json();

    assert.equal(createResponse.status, 201);
    assert.equal(createdShow.id, "1");
    assert.equal(createdShow.title, sampleShow.title);

    const getResponse = await context.request(`/api/shows/${createdShow.id}`, {
      method: "GET",
    });
    const fetchedShow = await getResponse.json();

    assert.equal(getResponse.status, 200);
    assert.equal(fetchedShow.description, sampleShow.description);
  } finally {
    await context.close();
  }
});

await runTest("reject invalid payloads with server-side validation", async () => {
  const context = await createTestContext();

  try {
    const response = await context.request("/api/shows", {
      method: "POST",
      body: JSON.stringify({
        ...sampleShow,
        title: "   ",
      }),
    });
    const body = await response.json();

    assert.equal(response.status, 400);
    assert.equal(body.message, "title is required.");
  } finally {
    await context.close();
  }
});

await runTest("list shows with pagination metadata", async () => {
  const context = await createTestContext();

  try {
    for (let index = 0; index < 3; index += 1) {
      await context.request("/api/shows", {
        method: "POST",
        body: JSON.stringify({
          ...sampleShow,
          title: `Show ${index + 1}`,
        }),
      });
    }

    const response = await context.request("/api/shows?page=2&limit=2", {
      method: "GET",
    });
    const body = await response.json();

    assert.equal(response.status, 200);
    assert.equal(body.items.length, 1);
    assert.equal(body.pagination.page, 2);
    assert.equal(body.pagination.limit, 2);
    assert.equal(body.pagination.totalItems, 3);
    assert.equal(body.pagination.totalPages, 2);
    assert.equal(body.pagination.hasNextPage, false);
    assert.equal(body.pagination.hasPreviousPage, true);
  } finally {
    await context.close();
  }
});

await runTest("replace and partially update a show", async () => {
  const context = await createTestContext();

  try {
    const createResponse = await context.request("/api/shows", {
      method: "POST",
      body: JSON.stringify(sampleShow),
    });
    const createdShow = await createResponse.json();

    const putResponse = await context.request(`/api/shows/${createdShow.id}`, {
      method: "PUT",
      body: JSON.stringify({
        ...sampleShow,
        title: "Blue Eye Samurai",
        status: "completed",
      }),
    });
    const replacedShow = await putResponse.json();

    assert.equal(putResponse.status, 200);
    assert.equal(replacedShow.title, "Blue Eye Samurai");
    assert.equal(replacedShow.status, "completed");

    const patchResponse = await context.request(`/api/shows/${createdShow.id}`, {
      method: "PATCH",
      body: JSON.stringify({
        rating: 10,
        episodesWatched: 9,
        totalEpisodes: 9,
      }),
    });
    const patchedShow = await patchResponse.json();

    assert.equal(patchResponse.status, 200);
    assert.equal(patchedShow.rating, 10);
    assert.equal(patchedShow.title, "Blue Eye Samurai");
  } finally {
    await context.close();
  }
});

await runTest("delete removes a show from the in-memory store", async () => {
  const context = await createTestContext();

  try {
    const createResponse = await context.request("/api/shows", {
      method: "POST",
      body: JSON.stringify(sampleShow),
    });
    const createdShow = await createResponse.json();

    const deleteResponse = await context.request(`/api/shows/${createdShow.id}`, {
      method: "DELETE",
    });

    assert.equal(deleteResponse.status, 204);
    assert.equal(context.store.list().length, 0);

    const getResponse = await context.request(`/api/shows/${createdShow.id}`, {
      method: "GET",
    });

    assert.equal(getResponse.status, 404);
  } finally {
    await context.close();
  }
});

await runTest("statistics endpoint aggregates current shows", async () => {
  const context = await createTestContext();

  try {
    await context.request("/api/shows", {
      method: "POST",
      body: JSON.stringify(sampleShow),
    });
    await context.request("/api/shows", {
      method: "POST",
      body: JSON.stringify({
        ...sampleShow,
        title: "The Batman",
        type: "movie",
        status: "completed",
        rating: 8,
      }),
    });

    const response = await context.request("/api/stats", { method: "GET" });
    const body = await response.json();

    assert.equal(response.status, 200);
    assert.equal(body.totalShows, 2);
    assert.equal(body.averageRating, 8.75);
    assert.deepEqual(body.byStatus, {
      watching: 1,
      completed: 1,
    });
    assert.deepEqual(body.byType, {
      series: 1,
      movie: 1,
    });
  } finally {
    await context.close();
  }
});

await runTest("reject invalid pagination query parameters", async () => {
  const context = await createTestContext();

  try {
    const response = await context.request("/api/shows?page=0&limit=500", {
      method: "GET",
    });
    const body = await response.json();

    assert.equal(response.status, 400);
    assert.equal(body.message, "page must be a positive integer.");
  } finally {
    await context.close();
  }
});
