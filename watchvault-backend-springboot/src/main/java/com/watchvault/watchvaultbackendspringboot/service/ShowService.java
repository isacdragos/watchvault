package com.watchvault.watchvaultbackendspringboot.service;

import com.watchvault.watchvaultbackendspringboot.dto.ShowRequest;
import com.watchvault.watchvaultbackendspringboot.dto.ShowResponse;
import com.watchvault.watchvaultbackendspringboot.entity.ShowEntity;
import com.watchvault.watchvaultbackendspringboot.entity.UserEntity;
import com.watchvault.watchvaultbackendspringboot.error.NotFoundException;
import com.watchvault.watchvaultbackendspringboot.repository.ShowRepository;
import com.watchvault.watchvaultbackendspringboot.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final UserRepository userRepository;

    public ShowService(ShowRepository showRepository, UserRepository userRepository) {
        this.showRepository = showRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<ShowResponse> listShows(String username, String status, String search, int page, int size) {
        UserEntity user = findUser(username);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean hasStatus = status != null && !status.isBlank();
        boolean hasSearch = search != null && !search.isBlank();

        Page<ShowEntity> result;

        if (hasStatus && hasSearch) {
            result = showRepository.findByUserIdAndStatusIgnoreCaseAndTitleContainingIgnoreCase(
                    user.getId(),
                    status,
                    search,
                    pageable
            );
        } else if (hasStatus) {
            result = showRepository.findByUserIdAndStatusIgnoreCase(user.getId(), status, pageable);
        } else if (hasSearch) {
            result = showRepository.findByUserIdAndTitleContainingIgnoreCase(user.getId(), search, pageable);
        } else {
            result = showRepository.findByUserId(user.getId(), pageable);
        }

        return result.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ShowResponse getShow(String username, Long id) {
        UserEntity user = findUser(username);

        ShowEntity show = showRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Show not found."));

        return toResponse(show);
    }

    @Transactional
    public ShowResponse createShow(String username, ShowRequest request) {
        UserEntity user = findUser(username);

        ShowEntity show = new ShowEntity();
        applyRequest(show, request);
        show.setUser(user);

        return toResponse(showRepository.save(show));
    }

    @Transactional
    public ShowResponse updateShow(String username, Long id, ShowRequest request) {
        UserEntity user = findUser(username);

        ShowEntity show = showRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Show not found."));

        applyRequest(show, request);
        return toResponse(showRepository.save(show));
    }

    @Transactional
    public void deleteShow(String username, Long id) {
        UserEntity user = findUser(username);

        ShowEntity show = showRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Show not found."));

        showRepository.delete(show);
    }

    private UserEntity findUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    private void applyRequest(ShowEntity show, ShowRequest request) {
        show.setTitle(request.getTitle());
        show.setType(request.getType());
        show.setStatus(request.getStatus());
        show.setDescription(request.getDescription());
        show.setReleaseDate(request.getReleaseDate());
        show.setImage(request.getImage());
        show.setEpisodesWatched(request.getEpisodesWatched());
        show.setTotalEpisodes(request.getTotalEpisodes());
        show.setRating(request.getRating());
    }

    private ShowResponse toResponse(ShowEntity show) {
        return new ShowResponse(
                show.getId(),
                show.getTitle(),
                show.getType(),
                show.getStatus(),
                show.getDescription(),
                show.getReleaseDate(),
                show.getImage(),
                show.getEpisodesWatched(),
                show.getTotalEpisodes(),
                show.getRating(),
                show.getCreatedAt(),
                show.getUpdatedAt()
        );
    }
}
