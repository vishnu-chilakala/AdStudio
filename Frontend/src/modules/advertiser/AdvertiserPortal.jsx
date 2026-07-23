import React, { useState } from "react";
import PageHeader from "../../components/PageHeader.jsx";
import DataTable from "../../components/DataTable.jsx";
import StatusBadge from "../../components/StatusBadge.jsx";
import Tabs from "../../components/Tabs.jsx";
import ProgressBar from "../../components/ProgressBar.jsx";
import { Loader, MockFlag } from "../../components/Loader.jsx";
import { useApiData } from "../../hooks/useApiData.js";
import { ENDPOINTS } from "../../api/endpoints.js";
import { IcAdvertiser, IcPlus, IcBuilding, IcEdit, IcClose } from "../../assets/icons.jsx";
import { MOCK_ADVERTISERS, MOCK_BRANDS } from "../../data/mockData.js";
import { formatCurrency, formatCompact } from "../../utils/format.js";

/* ---------------------------------------------------------------------- */
/*  Reusable modal shell                                                   */
/* ---------------------------------------------------------------------- */
function Modal({ title, onClose, children, width = 480 }) {
  return (
    <div className="modal-overlay" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal-panel" style={{ maxWidth: width }}>
        <div className="modal-head">
          <h3>{title}</h3>
          <button type="button" className="btn btn-ghost btn-icon" onClick={onClose} aria-label="Close">
            <IcClose size={16} />
          </button>
        </div>
        <div className="modal-body">{children}</div>
      </div>
    </div>
  );
}

/* ---------------------------------------------------------------------- */
/*  Advertiser create/edit form                                           */
/* ---------------------------------------------------------------------- */
function AdvertiserForm({ initial, onCancel, onSaved }) {
  const isEdit = Boolean(initial?.id);
  const [form, setForm] = useState({
    companyName: initial?.companyName || "",
    industry: initial?.industry || "",
    accountManager: initial?.accountManager || "",
    annualBudget: initial?.annualBudget ?? "",
    currency: initial?.currency || "USD",
    accountManagerId: initial0,
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const url = isEdit ? `${ENDPOINTS.advertisers}/${initial.id}` : ENDPOINTS.advertisers;
      const method = isEdit ? "PUT" : "POST";
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...form, annualBudget: Number(form.annualBudget) }),
      });
      if (!res.ok) throw new Error(`Request failed (${res.status})`);
      const saved = await res.json();
      onSaved(saved);
    } catch (err) {
      setError(err.message || "Something went wrong");
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={submit} className="form-grid">
      <label className="field">
        <span>Company name</span>
        <input required value={form.companyName} onChange={set("companyName")} placeholder="Acme Corp" />
      </label>
      <label className="field">
        <span>Industry</span>
        <input required value={form.industry} onChange={set("industry")} placeholder="Retail" />
      </label>
      <label className="field">
        <span>Account manager</span>
        <input required value={form.accountManager} onChange={set("accountManager")} placeholder="Jane Doe" />
      </label>
      <div className="field-row">
        <label className="field">
          <span>Annual budget</span>
          <input required type="number" min="0" step="0.01" value={form.annualBudget} onChange={set("annualBudget")} />
        </label>
        <label className="field">
          <span>Currency</span>
          <select value={form.currency} onChange={set("currency")}>
            <option value="USD">USD</option>
            <option value="EUR">EUR</option>
            <option value="GBP">GBP</option>
            <option value="INR">INR</option>
          </select>
        </label>
      </div>
      <label className="field">
        <span>Status</span>
        <select value={form.status} onChange={set("status")}>
          <option value="ACTIVE">Active</option>
          <option value="PAUSED">Paused</option>
          <option value="INACTIVE">Inactive</option>
        </select>
      </label>

      {error && <div className="form-error">{error}</div>}

      <div className="modal-actions">
        <button type="button" className="btn btn-ghost btn-sm" onClick={onCancel} disabled={saving}>Cancel</button>
        <button type="submit" className="btn btn-primary btn-sm" disabled={saving}>
          {saving ? "Saving..." : isEdit ? "Save changes" : "Create advertiser"}
        </button>
      </div>
    </form>
  );
}

/* ---------------------------------------------------------------------- */
/*  Brand create/edit form                                                */
/* ---------------------------------------------------------------------- */
function BrandForm({ initial, advertisers, onCancel, onSaved }) {
  const isEdit = Boolean(initial?.id);
  const [form, setForm] = useState({
    brandName: initial?.brandName || "",
    category: initial?.category || "",
    advertiserId: initial?.advertiserId || (advertisers?.[0]?.id ?? ""),
    allocatedBudget: initial?.allocatedBudget ?? "",
    spentToDate: initial?.spentToDate ?? 0,
    status: initial?.status || "ACTIVE",
    color: initial?.color || "#6366F1",
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const url = isEdit ? `${ENDPOINTS.brands}/${initial.id}` : ENDPOINTS.brands;
      const method = isEdit ? "PUT" : "POST";
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...form,
          allocatedBudget: Number(form.allocatedBudget),
          spentToDate: Number(form.spentToDate),
        }),
      });
      if (!res.ok) throw new Error(`Request failed (${res.status})`);
      const saved = await res.json();
      onSaved(saved);
    } catch (err) {
      setError(err.message || "Something went wrong");
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={submit} className="form-grid">
      <label className="field">
        <span>Brand name</span>
        <input required value={form.brandName} onChange={set("brandName")} placeholder="Solstice" />
      </label>
      <label className="field">
        <span>Category</span>
        <input required value={form.category} onChange={set("category")} placeholder="Beverages" />
      </label>
      <label className="field">
        <span>Advertiser</span>
        <select required value={form.advertiserId} onChange={set("advertiserId")}>
          {(advertisers || []).map((a) => (
            <option key={a.id} value={a.id}>{a.companyName}</option>
          ))}
        </select>
      </label>
      <div className="field-row">
        <label className="field">
          <span>Allocated budget</span>
          <input required type="number" min="0" step="0.01" value={form.allocatedBudget} onChange={set("allocatedBudget")} />
        </label>
        <label className="field">
          <span>Spent to date</span>
          <input type="number" min="0" step="0.01" value={form.spentToDate} onChange={set("spentToDate")} />
        </label>
      </div>
      <div className="field-row">
        <label className="field">
          <span>Status</span>
          <select value={form.status} onChange={set("status")}>
            <option value="ACTIVE">Active</option>
            <option value="PAUSED">Paused</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </label>
        <label className="field">
          <span>Color</span>
          <input type="color" value={form.color} onChange={set("color")} />
        </label>
      </div>

      {error && <div className="form-error">{error}</div>}

      <div className="modal-actions">
        <button type="button" className="btn btn-ghost btn-sm" onClick={onCancel} disabled={saving}>Cancel</button>
        <button type="submit" className="btn btn-primary btn-sm" disabled={saving}>
          {saving ? "Saving..." : isEdit ? "Save changes" : "Create brand"}
        </button>
      </div>
    </form>
  );
}

/* ---------------------------------------------------------------------- */
/*  Main page                                                              */
/* ---------------------------------------------------------------------- */
export default function AdvertiserPortal() {
  const [tab, setTab] = useState("advertisers");
  const { data: advertisers, loading: la, isMock, refetch: refetchAdvertisers } = useApiData(ENDPOINTS.advertisers, MOCK_ADVERTISERS);
  const { data: brands, loading: lb, refetch: refetchBrands } = useApiData(ENDPOINTS.brands, MOCK_BRANDS);

  const [advertiserModal, setAdvertiserModal] = useState(null); // null | {} (new) | advertiser (edit)
  const [brandModal, setBrandModal] = useState(null);

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
    { key: "actions", label: "", align: "right", render: (r) => (
      <div className="t-actions">
        <button className="btn btn-ghost btn-sm" onClick={() => setAdvertiserModal(r)}>
          <IcEdit size={15} /> Edit
        </button>
      </div>
    )},
  ];

  const tabs = [
    { key: "advertisers", label: "Advertisers", count: (advertisers || []).length },
    { key: "brands", label: "Brands", count: (brands || []).length },
  ];

  const handleAdvertiserSaved = () => {
    setAdvertiserModal(null);
    refetchAdvertisers?.();
  };

  const handleBrandSaved = () => {
    setBrandModal(null);
    refetchBrands?.();
  };

  return (
    <div className="page">
      <PageHeader
        Icon={IcAdvertiser}
        title="Advertisers & Brands"
        subtitle="Manage advertiser accounts, brand portfolios and budget headroom"
        actions={
          <>
            {isMock && <MockFlag />}
            <button
              className="btn btn-primary btn-sm"
              onClick={() => (tab === "brands" ? setBrandModal({}) : setAdvertiserModal({}))}
            >
              <IcPlus /> {tab === "brands" ? "New brand" : "New advertiser"}
            </button>
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
                <div className="brand-card" key={b.id} onClick={() => setBrandModal(b)}>
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

      {advertiserModal && (
        <Modal title={advertiserModal.id ? "Edit advertiser" : "New advertiser"} onClose={() => setAdvertiserModal(null)}>
          <AdvertiserForm
            initial={advertiserModal}
            onCancel={() => setAdvertiserModal(null)}
            onSaved={handleAdvertiserSaved}
          />
        </Modal>
      )}

      {brandModal && (
        <Modal title={brandModal.id ? "Edit brand" : "New brand"} onClose={() => setBrandModal(null)}>
          <BrandForm
            initial={brandModal}
            advertisers={advertisers}
            onCancel={() => setBrandModal(null)}
            onSaved={handleBrandSaved}
          />
        </Modal>
      )}
    </div>
  );
}