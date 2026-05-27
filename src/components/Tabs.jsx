import "./Tabs.css";

export function Tabs({ activeTab, setActiveTab }) {
  const tabs = [
    "All Shows",
    "Watching",
    "Completed",
    "On Hold",
    "Dropped",
    "Plan to watch",
  ];

  return (
    <div className="tabs">
      {tabs.map((tab) => (
        <span
          key={tab}
          className={activeTab === tab ? "active" : ""}
          onClick={() => setActiveTab(tab)}
        >
          {tab}
        </span>
      ))}
    </div>
  );
}