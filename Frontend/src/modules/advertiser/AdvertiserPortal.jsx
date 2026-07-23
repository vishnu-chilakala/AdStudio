import React, { useState } from "react";
import PageHeader from "../../components/PageHeader.jsx";
import DataTable from "../../components/DataTable.jsx";
import StatusBadge from "../../components/StatusBadge.jsx";
import Tabs from "../../components/Tabs.jsx";
import ProgressBar from "../../components/ProgressBar.jsx";
import { Loader, MockFlag } from "../../components/Loader.jsx";
import { useApiData } from "../../hooks/useApiData.js";
import { ENDPOINTS } from "../../api/endpoints.js";
import { IcAdvertiser, IcPlus, IcBuilding, IcEdit } from "../../assets/icons.jsx";
import { MOCK_ADVERTISERS, MOCK_BRANDS } from "../../data/mockData.js";
import { formatCurrency, formatCompact } from "../../utils/format.js";

export default function AdvertiserPortal() {
  const [tab, setTab] = useState("advertisers");
  const { data: advertisers, loading: la, isMock } = useApiData(ENDPOINTS.advertisers, MOCK_ADVERTISERS);
  const { data: brands, loading: lb } = useApiData(ENDPOINTS.brands, MOCK_BRANDS);

  const advColumns = [
    { key: "companyName", label: "Company", render: (r) => (
      <div className="id-chip">
        <span className="av"><IcBuilding size={17} /></span>
        <span className="meta"><span className="nm">{r.companyName}</span><span className="sb">{r.id} · {r.industry}</span></span>
      </div>
    )},
    { key: "accountManager", label: "Account Manager", render: (r) => <span className="cell-muted">{r.accountManager}</span> },
    { key: "annualBudget", label: "Annual Budget", align: "right", mono: true, render: (r) => <span className="strong">{formatCurrency(r.annualBudget, r.currency)}</span> },
    { key: "currency", label: "Currency", render: (r) => <span className="badge badge-gray">{r.currency}</span> },
    { key: "status", label: "Status", render: (r) => <StatusBadge status={r.status} /> },
    { key: "actions", label: "", align: "right", render: () => (
      <div className="t-actions"><button className="btn btn-ghost btn-sm"><IcEdit size={15} /> Edit</button></div>
    )},
  ];

  const tabs = [
    { key: "advertisers", label: "Advertisers", count: (advertisers || []).length },
    { key: "brands", label: "Brands", count: (brands || []).length },
  ];

  return (
    <div className="page">
      <PageHeader
        Icon={IcAdvertiser}
        title="Advertisers & Brands"
        subtitle="Manage advertiser accounts, brand portfolios and budget headroom"
        actions={
          <>
            {isMock && <MockFlag />}
            <button className="btn btn-primary btn-sm"><IcPlus /> {tab === "brands" ? "New brand" : "New advertiser"}</button>
          </>
        }
      />

      <div className="toolbar"><Tabs tabs={tabs} active={tab} onChange={setTab} /></div>

      {tab === "advertisers" && (
        <div className="card">
          {la ? <Loader /> : <DataTable columns={advColumns} rows={advertisers} />}
        </div>
      )}

      {tab === "brands" && (
        lb ? <Loader /> : (
          <div className="brand-grid">
            {(brands || []).map((b) => {
              const pct = b.allocatedBudget ? (b.spentToDate / b.allocatedBudget) * 100 : 0;
              const remaining = b.allocatedBudget - b.spentToDate;
              return (
                <div className="brand-card" key={b.id}>
                  <div className="bc-top">
                    <div className="bc-logo" style={{ background: b.color }}>{b.brandName[0]}</div>
                    <div>
                      <div className="bc-name">{b.brandName}</div>
                      <div className="bc-cat">{b.category} · {b.advertiser}</div>
                    </div>
                    <div style={{ marginLeft: "auto" }}><StatusBadge status={b.status} /></div>
                  </div>
                  <div className="bc-budget">
                    <span className="lab">Spent to date</span>
                    <span className="val">{formatCompact(b.spentToDate, { money: true })} / {formatCompact(b.allocatedBudget, { money: true })}</span>
                  </div>
                  <ProgressBar value={b.spentToDate} max={b.allocatedBudget} />
                  <div className="bc-foot">
                    <span className="rem">Remaining <b>{formatCompact(remaining, { money: true })}</b></span>
                    <span className="txt-sm mute">{Math.round(pct)}% used</span>
                  </div>
                </div>
              );
            })}
          </div>
        )
      )}
    </div>
  );
}
