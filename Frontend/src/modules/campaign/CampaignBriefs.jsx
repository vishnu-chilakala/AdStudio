import React, { useState } from "react";
import PageHeader from "../../components/PageHeader.jsx";
import DataTable from "../../components/DataTable.jsx";
import StatusBadge from "../../components/StatusBadge.jsx";
import Tabs from "../../components/Tabs.jsx";
import { Loader, MockFlag } from "../../components/Loader.jsx";
import { useApiData } from "../../hooks/useApiData.js";
import { ENDPOINTS } from "../../api/endpoints.js";
import { IcCampaign, IcPlus, IcCheck, IcClose, IcSend, IcUsers } from "../../assets/icons.jsx";
import { MOCK_BRIEFS, MOCK_AUDIENCES } from "../../data/mockData.js";
import { formatCompact } from "../../utils/format.js";

const OBJECTIVE_TONE = { Awareness: "badge-blue", Consideration: "badge-navy", Conversion: "badge-green", Retention: "badge-amber" };

export default function CampaignBriefs() {
  const [tab, setTab] = useState("briefs");
  const { data: briefs, loading: lb, isMock } = useApiData(ENDPOINTS.campaignBriefs, MOCK_BRIEFS);
  const { data: audiences, loading: la } = useApiData(ENDPOINTS.targetAudiences, MOCK_AUDIENCES);

  const briefColumns = [
    { key: "campaignName", label: "Campaign", render: (r) => (
      <span className="meta"><div className="strong">{r.campaignName}</div><div className="sb cell-muted">{r.id} · {r.brand}</div></span>
    )},
    { key: "objective", label: "Objective", render: (r) => <span className={`badge ${OBJECTIVE_TONE[r.objective] || "badge-gray"}`}>{r.objective}</span> },
    { key: "geography", label: "Geography", render: (r) => <span className="cell-muted">{r.geography}</span> },
    { key: "flight", label: "Flight", render: (r) => <span className="cell-muted cell-num">{r.startDate.slice(5)} → {r.endDate.slice(5)}</span> },
    { key: "totalBudget", label: "Budget", align: "right", mono: true, render: (r) => <span className="strong">{formatCompact(r.totalBudget, { money: true })}</span> },
    { key: "status", label: "Status", render: (r) => <StatusBadge status={r.status} /> },
    { key: "actions", label: "", align: "right", render: (r) => {
      if (r.status === "Draft") return <div className="t-actions"><button className="btn btn-outline btn-sm"><IcSend size={14} /> Submit</button></div>;
      if (r.status === "Submitted") return (
        <div className="t-actions">
          <button className="btn btn-success btn-sm"><IcCheck size={14} /> Approve</button>
          <button className="btn btn-danger btn-sm"><IcClose size={14} /> Reject</button>
        </div>
      );
      return <span className="cell-muted txt-sm">—</span>;
    }},
  ];

  const audColumns = [
    { key: "id", label: "Audience", render: (r) => (
      <div className="id-chip"><span className="av"><IcUsers size={16} /></span><span className="meta"><span className="nm">{r.id}</span><span className="sb">{r.brief}</span></span></div>
    )},
    { key: "ageRange", label: "Age", render: (r) => <span className="badge badge-gray">{r.ageRange}</span> },
    { key: "gender", label: "Gender", render: (r) => <span className="cell-muted">{r.gender}</span> },
    { key: "interests", label: "Interests", render: (r) => <span className="cell-muted">{r.interests}</span> },
    { key: "deviceType", label: "Device", render: (r) => <span className="badge badge-blue">{r.deviceType}</span> },
    { key: "status", label: "Status", render: (r) => <StatusBadge status={r.status} /> },
  ];

  const tabs = [
    { key: "briefs", label: "Campaign Briefs", count: (briefs || []).length },
    { key: "audiences", label: "Target Audiences", count: (audiences || []).length },
  ];

  return (
    <div className="page">
      <PageHeader
        Icon={IcCampaign}
        title="Campaign Planning & Briefing"
        subtitle="Capture briefs, objectives and target audiences, then run the approval workflow"
        actions={<>{isMock && <MockFlag />}<button className="btn btn-primary btn-sm"><IcPlus /> {tab === "audiences" ? "New audience" : "New brief"}</button></>}
      />

      <div className="toolbar"><Tabs tabs={tabs} active={tab} onChange={setTab} /></div>

      <div className="card">
        {tab === "briefs"
          ? (lb ? <Loader /> : <DataTable columns={briefColumns} rows={briefs} />)
          : (la ? <Loader /> : <DataTable columns={audColumns} rows={audiences} />)}
      </div>
    </div>
  );
}
