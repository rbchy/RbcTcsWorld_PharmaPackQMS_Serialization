import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';
import Login, { AuthUser } from './Login';
import HelpButton from './Help';
import {
  IconHome, IconDashboard, IconProduct, IconBatch, IconProduction, IconAql,
  IconReconciliation, IconQa, IconDeviation, IconCapa, IconSave, IconUser, IconLogout,
  IconSerial, IconAudit, IconSignature,
} from './icons';
import { FieldRule, fieldHandler, validateAll, validateDateOrder } from './validation';

// Dev server (vite, :5173) -> API on :8080 of the same host. Bundled desktop/server build -> same origin.
const API = (import.meta.env.VITE_API_URL as string | undefined)
  || (window.location.port === '5173' ? `${window.location.protocol}//${window.location.hostname}:8080/api` : `${window.location.origin}/api`);
const AUTH_STORAGE_KEY = 'pharmapack_qms_auth';

type Product = { id: number; productCode: string; productName: string; strength?: string; dosageForm?: string; packSize?: string; status: string };
type Batch = { id: number; batchNumber: string; productId: number; productCode: string; productName: string; lotSize: number; batchStatus: string };
type Deviation = { id: number; deviationNumber: string; batchId: number | null; batchNumber: string | null; productCode: string | null; productName: string | null; title: string; description: string; severity: string; status: string; openedBy: number | null; openedAt: string; closedAt: string | null };
type QAReview = { id: number; batchId: number; batchNumber: string; productCode: string; productName: string; reviewerId: number | null; reviewType: string; decision: string; reviewDate: string; comments: string | null };
type Reconciliation = { id: number; batchId: number; batchNumber: string; productCode: string; productName: string; startingQuantity: number; goodQuantity: number; rejectQuantity: number; unusedQuantity: number; reconciledQuantity: number; variance: number; status: string; calculatedAt: string; calculatedBy: number | null; remarks: string | null };
type CapaAction = { id: number; deviationId: number | null; deviationNumber: string | null; batchId: number | null; batchNumber: string | null; productCode: string | null; productName: string | null; actionType: string; actionDescription: string; ownerId: number | null; dueDate: string | null; completedDate: string | null; status: string; effectivenessResult: string | null };
type AqlPlan = { id: number; planCode: string; inspectionLevel: string; aqlValue: number; sampleSize: number; acceptanceNumber: number; rejectionNumber: number; description: string | null };
type AqlInspection = { id: number; batchId: number; batchNumber: string; productCode: string; productName: string; planId: number; planCode: string; inspectorId: number | null; inspectionTime: string; sampleSize: number; defectsFound: number; result: string; remarks: string | null };
type ProductionRun = { id: number; batchId: number | null; batchNumber: string | null; productCode: string | null; productName: string | null; lineId: number | null; lineCode: string | null; lineName: string | null; equipmentId: number | null; equipmentCode: string | null; equipmentName: string | null; startedAt: string | null; endedAt: string | null; status: string };
type PackagingLine = { id: number; lineCode: string; lineName: string; location?: string; status: string };
type Equipment = { id: number; equipmentCode: string; equipmentName: string; equipmentType?: string; status: string };
type ProductionEntry = { id: number; productionRunId: number | null; batchId: number | null; batchNumber: string | null; entryTime: string; quantityProduced: number; quantityGood: number; quantityReject: number; operatorId: number | null; remarks: string | null };
type SerializedUnit = { id: number; batchId: number | null; batchNumber: string | null; productCode: string | null; serialNumber: string; gtin: string | null; aggregationLevel: string; parentId: number | null; parentSerialNumber: string | null; status: string; commissionedAt: string | null; commissionedBy: number | null };
type AuditEntry = { id: number; username: string; actionType: string; entityName: string | null; entityId: string | null; fieldName: string | null; oldValue: string | null; newValue: string | null; createdAt: string };
type ESignature = { id: number; userId: number; username: string; fullName: string; entityName: string; entityId: string; actionType: string; signedAt: string; signatureReason: string | null };

function loadStoredAuth(): AuthUser | null {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch { return null; }
}
function storeAuth(auth: AuthUser | null) {
  try {
    if (auth) localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth));
    else localStorage.removeItem(AUTH_STORAGE_KEY);
  } catch { /* private-browsing / storage disabled: session still works, just won't persist across reloads */ }
}

function Table({ rows }: { rows: any[][] }) {
  return <table><tbody>{rows.map((r, i) => <tr key={i}>{r.map((c, j) => <td key={j}>{c ?? ''}</td>)}</tr>)}</tbody></table>;
}

// Phase 3B — one reusable text/number/date field: renders the label, the input,
// marks it invalid, and shows the inline error message under it. Every form below
// is built from this plus plain <select> blocks (selects keep their own markup
// because their options come from live data — products, batches, plans, etc.).
function TF({ label, value, error, onChange, onBlur, type = 'text', step, min, placeholder, testId }: {
  label: string; value: string; error?: string; onChange: (v: string) => void; onBlur?: () => void;
  type?: string; step?: string; min?: string; placeholder?: string; testId?: string;
}) {
  return (
    <label>
      {label}
      <input
        type={type} step={step} min={min} placeholder={placeholder} value={value} data-testid={testId}
        className={error ? 'field-invalid' : ''}
        onChange={e => onChange(e.target.value)}
        onBlur={onBlur}
      />
      {error && <span className="field-error">{error}</span>}
    </label>
  );
}
// Wraps a <select> (passed as children) with the same invalid/error treatment as TF.
function SF({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <label>
      {label}
      {React.isValidElement(children)
        ? React.cloneElement(children as React.ReactElement<any>, { className: [((children as React.ReactElement<any>).props.className || ''), error ? 'field-invalid' : ''].filter(Boolean).join(' ') })
        : children}
      {error && <span className="field-error">{error}</span>}
    </label>
  );
}

const TAB_ICONS: Record<string, (p: { size?: number }) => React.ReactElement> = {
  Dashboard: IconDashboard, Products: IconProduct, Batches: IconBatch, Production: IconProduction,
  AQL: IconAql, Reconciliation: IconReconciliation, 'QA Review': IconQa, Deviations: IconDeviation, CAPA: IconCapa,
  Serialization: IconSerial, 'Audit Trail': IconAudit, 'E-Signatures': IconSignature,
};
function TABS() {
  return ['Dashboard', 'Products', 'Batches', 'Production', 'AQL', 'Reconciliation', 'QA Review', 'Deviations', 'CAPA', 'Serialization', 'Audit Trail', 'E-Signatures'];
}

// ---- Phase 3B field rules — one object per form, used for validate-on-change,
// validate-on-blur and validate-on-submit alike (see validation.ts). ----
const PRODUCT_RULES: Record<string, FieldRule> = {
  productCode: { label: 'Product Code', type: 'code', required: true, capitalize: 'upper', maxLength: 40 },
  productName: { label: 'Product Name', type: 'name', required: true, capitalize: 'words', maxLength: 150 },
  strength: { label: 'Strength', type: 'name', maxLength: 40 },
  packSize: { label: 'Pack Size', type: 'name', maxLength: 40 },
};
const BATCH_RULES: Record<string, FieldRule> = {
  batchNumber: { label: 'Batch Number', type: 'code', required: true, capitalize: 'upper', maxLength: 40 },
  lotSize: { label: 'Lot Size', type: 'decimal', required: true, min: 0.001 },
  manufacturingDate: { label: 'Manufacturing Date', type: 'date' },
  expiryDate: { label: 'Expiry Date', type: 'date' },
};
const DEVIATION_RULES: Record<string, FieldRule> = {
  deviationNumber: { label: 'Deviation Number', type: 'code', required: true, capitalize: 'upper', maxLength: 40 },
  title: { label: 'Title', type: 'name', required: true, capitalize: 'words', maxLength: 150 },
  description: { label: 'Description', type: 'text', required: true, capitalize: 'first', maxLength: 2000 },
  openedBy: { label: 'Opened By (user id)', type: 'integer', min: 1 },
};
const QA_RULES: Record<string, FieldRule> = {
  reviewerId: { label: 'Reviewer (user id)', type: 'integer', min: 1 },
  comments: { label: 'Comments', type: 'text', capitalize: 'first', maxLength: 2000 },
};
const RECON_RULES: Record<string, FieldRule> = {
  startingQuantity: { label: 'Starting Quantity', type: 'decimal', required: true, min: 0 },
  goodQuantity: { label: 'Good Quantity', type: 'decimal', min: 0 },
  rejectQuantity: { label: 'Reject Quantity', type: 'decimal', min: 0 },
  unusedQuantity: { label: 'Unused Quantity', type: 'decimal', min: 0 },
  calculatedBy: { label: 'Calculated By (user id)', type: 'integer', min: 1 },
  remarks: { label: 'Remarks', type: 'text', capitalize: 'first', maxLength: 2000 },
};
const CAPA_RULES: Record<string, FieldRule> = {
  dueDate: { label: 'Due Date', type: 'date' },
  ownerId: { label: 'Owner (user id)', type: 'integer', min: 1 },
  actionDescription: { label: 'Action Description', type: 'text', required: true, capitalize: 'first', maxLength: 2000 },
};
const AQL_RULES: Record<string, FieldRule> = {
  sampleSize: { label: 'Sample Size', type: 'integer', min: 1 },
  defectsFound: { label: 'Defects Found', type: 'integer', required: true, min: 0 },
  inspectorId: { label: 'Inspector (user id)', type: 'integer', min: 1 },
  remarks: { label: 'Remarks', type: 'text', capitalize: 'first', maxLength: 2000 },
};
const ENTRY_RULES: Record<string, FieldRule> = {
  produced: { label: 'Produced', type: 'decimal', required: true, min: 0 },
  good: { label: 'Good', type: 'decimal', min: 0 },
  reject: { label: 'Reject', type: 'decimal', min: 0 },
  operatorId: { label: 'Operator (user id)', type: 'integer', min: 1 },
  remarks: { label: 'Remarks', type: 'text', capitalize: 'first', maxLength: 2000 },
};

const SERIAL_RULES: Record<string, FieldRule> = {
  quantity: { label: 'Quantity', type: 'integer', required: true, min: 1, max: 100000 },
  gtin: { label: 'GTIN', type: 'code', maxLength: 20 },
};
const AGG_RULES: Record<string, FieldRule> = {
  parentSerialNumber: { label: 'Parent Serial Number', type: 'code', required: true, maxLength: 120 },
};
const SIGN_RULES: Record<string, FieldRule> = {
  entityId: { label: 'Record ID', type: 'code', required: true, maxLength: 100 },
  signatureReason: { label: 'Signature Reason', type: 'text', capitalize: 'first', maxLength: 500 },
};

const DOSAGE_FORMS = ['Tablet', 'Capsule', 'Syrup', 'Injection', 'Ointment', 'Cream', 'Suspension', 'Powder', 'Inhaler', 'Other'];

function App() {
  const [auth, setAuth] = useState<AuthUser | null>(() => loadStoredAuth());
  const [tab, setTab] = useState('Dashboard');
  const [msg, setMsg] = useState('');
  const [msgKind, setMsgKind] = useState<'ok' | 'error'>('ok');

  const [products, setProducts] = useState<Product[]>([]);
  const [batches, setBatches] = useState<Batch[]>([]);
  const [deviations, setDeviations] = useState<Deviation[]>([]);
  const [qaReviews, setQaReviews] = useState<QAReview[]>([]);
  const [reconciliations, setReconciliations] = useState<Reconciliation[]>([]);
  const [capaActions, setCapaActions] = useState<CapaAction[]>([]);
  const [aqlPlans, setAqlPlans] = useState<AqlPlan[]>([]);
  const [aqlInspections, setAqlInspections] = useState<AqlInspection[]>([]);
  const [productionRuns, setProductionRuns] = useState<ProductionRun[]>([]);
  const [entries, setEntries] = useState<ProductionEntry[]>([]);
  const [entriesRunId, setEntriesRunId] = useState('');
  const [lines, setLines] = useState<PackagingLine[]>([]);
  const [equipmentList, setEquipmentList] = useState<Equipment[]>([]);

  const [pf, setPf] = useState({ productCode: '', productName: '', strength: '', dosageForm: 'Tablet', packSize: '', status: 'ACTIVE' });
  const [pfErrors, setPfErrors] = useState<Record<string, string>>({});
  const [bf, setBf] = useState({ batchNumber: '', productId: '', lotSize: '', manufacturingDate: '', expiryDate: '' });
  const [bfErrors, setBfErrors] = useState<Record<string, string>>({});
  const [devf, setDevf] = useState({ deviationNumber: '', batchId: '', title: '', description: '', severity: 'MINOR', openedBy: '' });
  const [devfErrors, setDevfErrors] = useState<Record<string, string>>({});
  const [qaf, setQaf] = useState({ batchId: '', reviewerId: '', reviewType: 'IPQC', decision: 'PASS', comments: '' });
  const [qafErrors, setQafErrors] = useState<Record<string, string>>({});
  const [recf, setRecf] = useState({ batchId: '', startingQuantity: '', goodQuantity: '', rejectQuantity: '', unusedQuantity: '', calculatedBy: '', remarks: '' });
  const [recfErrors, setRecfErrors] = useState<Record<string, string>>({});
  const [capaf, setCapaf] = useState({ deviationId: '', actionType: 'CORRECTIVE', actionDescription: '', ownerId: '', dueDate: '' });
  const [capafErrors, setCapafErrors] = useState<Record<string, string>>({});
  const [aqlf, setAqlf] = useState({ batchId: '', planId: '', inspectorId: '', sampleSize: '', defectsFound: '0', remarks: '' });
  const [aqlfErrors, setAqlfErrors] = useState<Record<string, string>>({});
  const [runf, setRunf] = useState({ batchId: '', lineId: '', equipmentId: '' });
  const [runfErrors, setRunfErrors] = useState<Record<string, string>>({});
  const [entryf, setEntryf] = useState({ runId: '', produced: '', good: '', reject: '', operatorId: '', remarks: '' });
  const [entryfErrors, setEntryfErrors] = useState<Record<string, string>>({});

  const [serializedUnits, setSerializedUnits] = useState<SerializedUnit[]>([]);
  const [serialBatchId, setSerialBatchId] = useState('');
  const [serf, setSerf] = useState({ batchId: '', quantity: '10', aggregationLevel: 'UNIT', gtin: '' });
  const [serfErrors, setSerfErrors] = useState<Record<string, string>>({});
  const [aggf, setAggf] = useState({ parentSerialNumber: '', childSerialNumbers: '' });
  const [aggfErrors, setAggfErrors] = useState<Record<string, string>>({});
  const [lineStatus, setLineStatus] = useState<{plcState: string; devices: Record<string,string>}>({ plcState: 'STOPPED', devices: {} });
  const [visionf, setVisionf] = useState({ serialNumber: '', actualBarcode: '', actualLot: '', actualExpiry: '' });
  const [decomf, setDecomf] = useState({ serialNumber: '', reason: 'DAMAGED_PACKAGE', comment: '' });
  const [auditTrail, setAuditTrail] = useState<AuditEntry[]>([]);
  const [signatures, setSignatures] = useState<ESignature[]>([]);
  const [sigf, setSigf] = useState({ entityName: 'QAReview', entityId: '', actionType: 'APPROVE', signatureReason: '' });
  const [sigfErrors, setSigfErrors] = useState<Record<string, string>>({});

  const pfChange = fieldHandler(pf, setPf, PRODUCT_RULES, pfErrors, setPfErrors);
  const bfChange = fieldHandler(bf, setBf, BATCH_RULES, bfErrors, setBfErrors);
  const devfChange = fieldHandler(devf, setDevf, DEVIATION_RULES, devfErrors, setDevfErrors);
  const qafChange = fieldHandler(qaf, setQaf, QA_RULES, qafErrors, setQafErrors);
  const recfChange = fieldHandler(recf, setRecf, RECON_RULES, recfErrors, setRecfErrors);
  const capafChange = fieldHandler(capaf, setCapaf, CAPA_RULES, capafErrors, setCapafErrors);
  const aqlfChange = fieldHandler(aqlf, setAqlf, AQL_RULES, aqlfErrors, setAqlfErrors);
  const entryfChange = fieldHandler(entryf, setEntryf, ENTRY_RULES, entryfErrors, setEntryfErrors);
  const serfChange = fieldHandler(serf, setSerf, SERIAL_RULES, serfErrors, setSerfErrors);
  const aggfChange = fieldHandler(aggf, setAggf, AGG_RULES, aggfErrors, setAggfErrors);
  const sigfChange = fieldHandler(sigf, setSigf, SIGN_RULES, sigfErrors, setSigfErrors);

  // Attaches the JWT to every API call; a 401 (expired/invalid token) logs the user out
  // and drops them back on the Login screen instead of showing a confusing error.
  const api = async (path: string, opts: RequestInit = {}) => {
    const headers: Record<string, string> = { 'Content-Type': 'application/json', ...(opts.headers as any || {}) };
    if (auth?.token) headers['Authorization'] = 'Bearer ' + auth.token;
    const r = await fetch(API + path, { headers, ...opts });
    if (r.status === 401) { logout(); throw Error('Your session has expired. Please sign in again.'); }
    if (!r.ok) { const x = await r.json().catch(() => ({ message: r.statusText })); throw Error(x.message || r.statusText); }
    return r.status === 204 ? null : r.json();
  };

  const notify = (text: string, kind: 'ok' | 'error' = 'ok') => { setMsg(text); setMsgKind(kind); };

  const load = () => Promise.all([
    api('/products'), api('/batches'), api('/deviations'), api('/qa-reviews'),
    api('/reconciliations'), api('/capa'), api('/aql/plans'), api('/aql/inspections'), api('/production/runs'),
    api('/master/lines'), api('/master/equipment'),
  ]).then(([p, b, dv, qa, rec, capa, plans, insp, runs, lns, eqp]) => {
    setProducts(p); setBatches(b); setDeviations(dv); setQaReviews(qa);
    setReconciliations(rec); setCapaActions(capa); setAqlPlans(plans); setAqlInspections(insp); setProductionRuns(runs);
    setLines(lns); setEquipmentList(eqp);
  }).catch(e => notify(e.message, 'error'));

  useEffect(() => { if (auth) load(); }, [auth?.token]);
  useEffect(() => { if (auth && tab === 'Audit Trail') api('/audit-trails').then(setAuditTrail).catch(e => notify(e.message, 'error')); }, [auth?.token, tab]);
  useEffect(() => { if (auth && tab === 'E-Signatures') api('/esignatures').then(setSignatures).catch(e => notify(e.message, 'error')); }, [auth?.token, tab]);

  const handleLogin = (u: AuthUser) => { storeAuth(u); setAuth(u); };
  const logout = () => { storeAuth(null); setAuth(null); setTab('Dashboard'); };

  if (!auth) return <Login onLogin={handleLogin} />;

  const INVALID_MSG = 'Please fix the highlighted field(s) before saving.';

  const saveProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validateAll(PRODUCT_RULES, pf);
    setPfErrors(errors);
    if (Object.keys(errors).length) { notify(INVALID_MSG, 'error'); return; }
    notify('');
    try { await api('/products', { method: 'POST', body: JSON.stringify(pf) }); setPf({ ...pf, productCode: '', productName: '' }); await load(); notify('Product saved'); }
    catch (x: any) { notify(x.message, 'error'); }
  };

  const saveBatch = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validateAll(BATCH_RULES, bf);
    if (!bf.productId) errors.productId = 'Please select a product.';
    const dateErr = validateDateOrder('the manufacturing date', 'Expiry Date', bf.manufacturingDate, bf.expiryDate);
    if (dateErr) errors.expiryDate = dateErr;
    setBfErrors(errors);
    if (Object.keys(errors).length) { notify(INVALID_MSG, 'error'); return; }
    notify('');
    try { await api('/batches', { method: 'POST', body: JSON.stringify({ ...bf, productId: Number(bf.productId), lotSize: Number(bf.lotSize) }) }); setBf({ ...bf, batchNumber: '', lotSize: '' }); await load(); notify('Batch saved'); }
    catch (x: any) { notify(x.message, 'error'); }
  };

  const saveDeviation = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validateAll(DEVIATION_RULES, devf);
    setDevfErrors(errors);
    if (Object.keys(errors).length) { notify(INVALID_MSG, 'error'); return; }
    notify('');
    try {
      await api('/deviations', { method: 'POST', body: JSON.stringify({ ...devf, batchId: devf.batchId ? Number(devf.batchId) : null, openedBy: devf.openedBy ? Number(devf.openedBy) : null }) });
      setDevf({ ...devf, deviationNumber: '', title: '', description: '' }); await load(); notify('Deviation recorded');
    } catch (x: any) { notify(x.message, 'error'); }
  };

  const saveQaReview = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validateAll(QA_RULES, qaf);
    if (!qaf.batchId) errors.batchId = 'Please select a batch.';
    setQafErrors(errors);
    if (Object.keys(errors).length) { notify(INVALID_MSG, 'error'); return; }
    notify('');
    try {
      await api('/qa-reviews', { method: 'POST', body: JSON.stringify({ ...qaf, batchId: Number(qaf.batchId), reviewerId: qaf.reviewerId ? Number(qaf.reviewerId) : null }) });
      setQaf({ ...qaf, batchId: '', comments: '' }); await load(); notify('QA review recorded');
    } catch (x: any) { notify(x.message, 'error'); }
  };

  const saveReconciliation = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validateAll(RECON_RULES, recf);
    if (!recf.batchId) errors.batchId = 'Please select a batch.';
    const start = Number(recf.startingQuantity || 0);
    const sumOut = Number(recf.goodQuantity || 0) + Number(recf.rejectQuantity || 0) + Number(recf.unusedQuantity || 0);
    if (!errors.startingQuantity && !errors.goodQuantity && !errors.rejectQuantity && !errors.unusedQuantity && sumOut > start) {
      errors.unusedQuantity = 'Good + Reject + Unused cannot exceed the Starting Quantity.';
    }
    setRecfErrors(errors);
    if (Object.keys(errors).length) { notify(INVALID_MSG, 'error'); return; }
    notify('');
    try {
      await api('/reconciliations', {
        method: 'POST', body: JSON.stringify({
          batchId: Number(recf.batchId),
          startingQuantity: Number(recf.startingQuantity),
          goodQuantity: Number(recf.goodQuantity || 0),
          rejectQuantity: Number(recf.rejectQuantity || 0),
          unusedQuantity: Number(recf.unusedQuantity || 0),
          calculatedBy: recf.calculatedBy ? Number(recf.calculatedBy) : null,
          remarks: recf.remarks,
        }),
      });
      await load(); notify('Reconciliation calculated');
    } catch (x: any) { notify(x.message, 'error'); }
  };

  const saveCapa = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validateAll(CAPA_RULES, capaf);
    if (!capaf.deviationId) errors.deviationId = 'Please select a deviation.';
    setCapafErrors(errors);
    if (Object.keys(errors).length) { notify(INVALID_MSG, 'error'); return; }
    notify('');
    try {
      await api('/capa', { method: 'POST', body: JSON.stringify({ ...capaf, deviationId: Number(capaf.deviationId), ownerId: capaf.ownerId ? Number(capaf.ownerId) : null, dueDate: capaf.dueDate || null }) });
      setCapaf({ ...capaf, actionDescription: '', dueDate: '' }); await load(); notify('CAPA action recorded');
    } catch (x: any) { notify(x.message, 'error'); }
  };

  const saveAql = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validateAll(AQL_RULES, aqlf);
    if (!aqlf.batchId) errors.batchId = 'Please select a batch.';
    if (!aqlf.planId) errors.planId = 'Please select an AQL plan.';
    setAqlfErrors(errors);
    if (Object.keys(errors).length) { notify(INVALID_MSG, 'error'); return; }
    notify('');
    try {
      await api('/aql/inspections', {
        method: 'POST', body: JSON.stringify({
          batchId: Number(aqlf.batchId), planId: Number(aqlf.planId),
          inspectorId: aqlf.inspectorId ? Number(aqlf.inspectorId) : null,
          sampleSize: aqlf.sampleSize ? Number(aqlf.sampleSize) : null,
          defectsFound: Number(aqlf.defectsFound || 0), remarks: aqlf.remarks,
        }),
      });
      setAqlf({ ...aqlf, defectsFound: '0', remarks: '' }); await load(); notify('AQL inspection recorded');
    } catch (x: any) { notify(x.message, 'error'); }
  };

  const saveRun = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors: Record<string, string> = {};
    if (!runf.batchId) errors.batchId = 'Please select a batch.';
    if (!runf.lineId) errors.lineId = 'Please select a packaging line.';
    setRunfErrors(errors);
    if (Object.keys(errors).length) { notify(INVALID_MSG, 'error'); return; }
    notify('');
    try {
      await api('/production/runs', {
        method: 'POST', body: JSON.stringify({
          batchId: Number(runf.batchId), lineId: Number(runf.lineId),
          equipmentId: runf.equipmentId ? Number(runf.equipmentId) : null,
        }),
      });
      setRunf({ batchId: '', lineId: '', equipmentId: '' }); await load(); notify('Production run started');
    } catch (x: any) { notify(x.message, 'error'); }
  };

  const saveEntry = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validateAll(ENTRY_RULES, entryf);
    if (!entryf.runId) errors.runId = 'Please select a production run.';
    const produced = Number(entryf.produced || 0);
    const goodPlusReject = Number(entryf.good || 0) + Number(entryf.reject || 0);
    if (!errors.produced && !errors.good && !errors.reject && goodPlusReject > produced) {
      errors.reject = 'Good + Reject cannot exceed the Produced quantity.';
    }
    setEntryfErrors(errors);
    if (Object.keys(errors).length) { notify(INVALID_MSG, 'error'); return; }
    notify('');
    try {
      await api('/production/entries', {
        method: 'POST', body: JSON.stringify({
          runId: Number(entryf.runId), produced: Number(entryf.produced), good: Number(entryf.good || 0), reject: Number(entryf.reject || 0),
          operatorId: entryf.operatorId ? Number(entryf.operatorId) : null, remarks: entryf.remarks,
        }),
      });
      setEntryf({ ...entryf, produced: '', good: '', reject: '', remarks: '' }); notify('Production entry recorded');
      if (entriesRunId === entryf.runId) await loadEntries(entryf.runId);
    } catch (x: any) { notify(x.message, 'error'); }
  };

  const loadEntries = async (runId: string) => {
    setEntriesRunId(runId);
    if (!runId) { setEntries([]); return; }
    try { setEntries(await api(`/production/runs/${runId}/entries`)); } catch (x: any) { notify(x.message, 'error'); }
  };

  const loadSerialization = async (batchId: string) => {
    setSerialBatchId(batchId);
    if (!batchId) { setSerializedUnits([]); return; }
    try { setSerializedUnits(await api(`/serialization/batch/${batchId}`)); } catch (x: any) { notify(x.message, 'error'); }
  };

  const commissionSerials = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validateAll(SERIAL_RULES, serf);
    if (!serf.batchId) errors.batchId = 'Please select a batch.';
    setSerfErrors(errors);
    if (Object.keys(errors).length) { notify(INVALID_MSG, 'error'); return; }
    notify('');
    try {
      await api('/serialization/commission', {
        method: 'POST', body: JSON.stringify({
          batchId: Number(serf.batchId), quantity: Number(serf.quantity),
          aggregationLevel: serf.aggregationLevel, gtin: serf.gtin || null,
        }),
      });
      notify('Serial numbers commissioned');
      if (serialBatchId === serf.batchId) await loadSerialization(serf.batchId);
    } catch (x: any) { notify(x.message, 'error'); }
  };

  const loadLineStatus = async () => {
    try { setLineStatus(await api('/line/status')); } catch (x: any) { notify(x.message, 'error'); }
  };

  const lineAction = async (action: string) => {
    // DEF-05: the command response is only {plcState}; merge it instead of replacing (replacing dropped
    // `devices`, and lineStatus.devices['PRINTER-01'] then crashed the whole page after START LINE).
    try { const r = await api(`/line/${action}`, { method: 'POST' }); setLineStatus(s => ({ ...s, ...r, devices: r.devices ?? s.devices ?? {} })); notify(`Line ${action.replace('-', ' ')} command accepted`); }
    catch (x: any) { notify(x.message, 'error'); }
  };

  const verifyVision = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api('/serialization/verify', { method: 'POST', body: JSON.stringify(visionf) });
      notify('Vision verification completed');
      if (serialBatchId) await loadSerialization(serialBatchId);
    } catch (x: any) { notify(x.message, 'error'); }
  };

  const printSerial = async (serial: string) => {
    try { await api(`/serialization/print/${encodeURIComponent(serial)}`, { method: 'POST' }); notify('Serial marked PRINTED'); if (serialBatchId) await loadSerialization(serialBatchId); }
    catch (x: any) { notify(x.message, 'error'); }
  };

  const commissionSerial = async (serial: string) => {
    try { await api(`/serialization/commission/${encodeURIComponent(serial)}`, { method: 'POST' }); notify('Serial COMMISSIONED'); if (serialBatchId) await loadSerialization(serialBatchId); }
    catch (x: any) { notify(x.message, 'error'); }
  };

  const decommissionSerial = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api('/serialization/decommission', { method: 'POST', body: JSON.stringify(decomf) });
      notify('Controlled decommission event recorded');
      setDecomf({ serialNumber: '', reason: 'DAMAGED_PACKAGE', comment: '' });
      if (serialBatchId) await loadSerialization(serialBatchId);
    } catch (x: any) { notify(x.message, 'error'); }
  };

  const aggregateSerials = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validateAll(AGG_RULES, aggf);
    const children = aggf.childSerialNumbers.split(',').map(s => s.trim()).filter(Boolean);
    if (!children.length) errors.childSerialNumbers = 'Enter at least one child serial number, comma-separated.';
    setAggfErrors(errors);
    if (Object.keys(errors).length) { notify(INVALID_MSG, 'error'); return; }
    notify('');
    try {
      await api('/serialization/aggregate', { method: 'POST', body: JSON.stringify({ parentSerialNumber: aggf.parentSerialNumber.trim(), childSerialNumbers: children }) });
      setAggf({ parentSerialNumber: '', childSerialNumbers: '' }); notify('Aggregation recorded');
      if (serialBatchId) await loadSerialization(serialBatchId);
    } catch (x: any) { notify(x.message, 'error'); }
  };

  const saveSignature = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validateAll(SIGN_RULES, sigf);
    setSigfErrors(errors);
    if (Object.keys(errors).length) { notify(INVALID_MSG, 'error'); return; }
    notify('');
    try {
      await api('/esignatures', { method: 'POST', body: JSON.stringify(sigf) });
      setSigf({ ...sigf, entityId: '', signatureReason: '' });
      setSignatures(await api('/esignatures')); notify('Signature recorded');
    } catch (x: any) { notify(x.message, 'error'); }
  };

  return <div>
    <header>
      <div className="watercolor-banner watercolor-banner--compact" style={{ borderRadius: 0 }}>
        <div className="watercolor-banner__content" style={{ color: 'white' }}>
          <h1 style={{ color: 'white', textShadow: '0 1px 4px #00000055' }}>RbcTcsWorld &middot; PharmaPack QMS</h1>
          <p style={{ color: '#ffffffdd' }}>Pharmaceutical Packaging Quality Management &mdash; portfolio / training application</p>
        </div>
      </div>
    </header>
    <nav>{TABS().map(x => {
      const Icon = TAB_ICONS[x];
      return <button key={x} data-testid={'nav-' + x.toLowerCase().replace(/\s+/g, '-')} className={tab === x ? 'active' : ''} onClick={() => setTab(x)}><Icon size={15} />{x}</button>;
    })}</nav>
    <main>
      <div className="topbar">
        <div className="topbar__left">
          <button className="chip-btn home" onClick={() => setTab('Dashboard')}><IconHome size={15} /> Home</button>
          <HelpButton topic={tab} />
        </div>
        <div className="topbar__right">
          <span className="user-pill"><IconUser size={14} /> <strong>{auth.fullName}</strong> {auth.roles[0] && <span className="role-badge">{auth.roles[0]}</span>}</span>
          <button data-testid="logout" className="chip-btn logout" onClick={logout}><IconLogout size={15} /> Logout</button>
        </div>
      </div>

      {msg && <div data-testid="notice" data-kind={msgKind} className={'notice' + (msgKind === 'error' ? ' error' : '')}>{msg}</div>}

      {tab === 'Dashboard' && <>
        <div className="cards">
          <div>Products<strong>{products.length}</strong></div>
          <div>Batches<strong>{batches.length}</strong></div>
          <div>Open Deviations<strong>{deviations.filter(d => d.status === 'OPEN').length}</strong></div>
          <div>Backend<strong>MySQL</strong></div>
        </div>
        <h2>Workflow</h2>
        <p>Product → Material → Batch → Line Clearance → Production → AQL → Reconciliation → QA Review → Deviation/CAPA</p>
      </>}

      {tab === 'Products' && <>
        <h2>Product Master</h2>
        <form onSubmit={saveProduct} className="form">
          <TF label="Product Code*" value={pf.productCode} error={pfErrors.productCode} onChange={v => pfChange('productCode')(v)} placeholder="e.g. PARA-500" />
          <TF label="Product Name*" value={pf.productName} error={pfErrors.productName} onChange={v => pfChange('productName')(v)} placeholder="e.g. Paracetamol" />
          <TF label="Strength" value={pf.strength} error={pfErrors.strength} onChange={v => pfChange('strength')(v)} placeholder="e.g. 500 mg" />
          <SF label="Dosage Form">
            <select value={pf.dosageForm} onChange={e => setPf({ ...pf, dosageForm: e.target.value })}>
              {DOSAGE_FORMS.map(d => <option key={d} value={d}>{d}</option>)}
            </select>
          </SF>
          <TF label="Pack Size" value={pf.packSize} error={pfErrors.packSize} onChange={v => pfChange('packSize')(v)} placeholder="e.g. 10 x 10" />
          <button className="primary btn-icon"><IconSave size={16} /> Save Product</button>
        </form>
        <Table rows={products.map(p => [p.id, p.productCode, p.productName, p.strength || '', p.dosageForm || '', p.packSize || '', p.status])} />
      </>}

      {tab === 'Batches' && <>
        <h2>Batch Master</h2>
        <form onSubmit={saveBatch} className="form">
          <TF label="Batch Number*" value={bf.batchNumber} error={bfErrors.batchNumber} onChange={v => bfChange('batchNumber')(v)} placeholder="e.g. BATCH-2026-001" />
          <SF label="Product*" error={bfErrors.productId}>
            <select value={bf.productId} onChange={e => setBf({ ...bf, productId: e.target.value })}>
              <option value="">Select</option>{products.map(p => <option key={p.id} value={p.id}>{p.productCode} - {p.productName}</option>)}
            </select>
          </SF>
          <TF label="Lot Size*" type="number" step="0.001" min="0.001" value={bf.lotSize} error={bfErrors.lotSize} onChange={v => bfChange('lotSize')(v)} />
          <TF label="Manufacturing Date" type="date" value={bf.manufacturingDate} error={bfErrors.manufacturingDate} onChange={v => bfChange('manufacturingDate')(v)} />
          <TF label="Expiry Date" type="date" value={bf.expiryDate} error={bfErrors.expiryDate} onChange={v => bfChange('expiryDate')(v)} />
          <button className="primary btn-icon"><IconSave size={16} /> Create Batch</button>
        </form>
        <Table rows={batches.map(b => [b.id, b.batchNumber, b.productCode, b.productName, b.lotSize, b.batchStatus])} />
      </>}

      {tab === 'Deviations' && <>
        <h2>Deviations</h2>
        <form onSubmit={saveDeviation} className="form">
          <TF label="Deviation Number*" value={devf.deviationNumber} error={devfErrors.deviationNumber} onChange={v => devfChange('deviationNumber')(v)} placeholder="e.g. DEV-2026-001" />
          <SF label="Batch">
            <select value={devf.batchId} onChange={e => setDevf({ ...devf, batchId: e.target.value })}>
              <option value="">(none)</option>{batches.map(b => <option key={b.id} value={b.id}>{b.batchNumber}</option>)}
            </select>
          </SF>
          <TF label="Title*" value={devf.title} error={devfErrors.title} onChange={v => devfChange('title')(v)} />
          <SF label="Severity*">
            <select value={devf.severity} onChange={e => setDevf({ ...devf, severity: e.target.value })}>
              <option value="MINOR">MINOR</option><option value="MAJOR">MAJOR</option><option value="CRITICAL">CRITICAL</option>
            </select>
          </SF>
          <TF label="Description*" value={devf.description} error={devfErrors.description} onChange={v => devfChange('description')(v)} />
          <TF label="Opened By (user id)" type="number" min="1" value={devf.openedBy} error={devfErrors.openedBy} onChange={v => devfChange('openedBy')(v)} />
          <button className="primary btn-icon"><IconSave size={16} /> Log Deviation</button>
        </form>
        <Table rows={deviations.map(d => [d.id, d.deviationNumber, d.batchNumber || '', d.title, d.severity, d.status, d.openedAt])} />
      </>}

      {tab === 'QA Review' && <>
        <h2>QA Review</h2>
        <form onSubmit={saveQaReview} className="form">
          <SF label="Batch*" error={qafErrors.batchId}>
            <select value={qaf.batchId} onChange={e => setQaf({ ...qaf, batchId: e.target.value })}>
              <option value="">Select</option>{batches.map(b => <option key={b.id} value={b.id}>{b.batchNumber}</option>)}
            </select>
          </SF>
          <SF label="Review Type*">
            <select value={qaf.reviewType} onChange={e => setQaf({ ...qaf, reviewType: e.target.value })}>
              <option value="IPQC">IPQC</option><option value="FINAL">FINAL</option><option value="LINE_CLEARANCE">LINE_CLEARANCE</option>
            </select>
          </SF>
          <SF label="Decision*">
            <select value={qaf.decision} onChange={e => setQaf({ ...qaf, decision: e.target.value })}>
              <option value="PASS">PASS</option><option value="FAIL">FAIL</option><option value="PENDING">PENDING</option>
            </select>
          </SF>
          <TF label="Reviewer (user id)" type="number" min="1" value={qaf.reviewerId} error={qafErrors.reviewerId} onChange={v => qafChange('reviewerId')(v)} />
          <TF label="Comments" value={qaf.comments} error={qafErrors.comments} onChange={v => qafChange('comments')(v)} />
          <button className="primary btn-icon"><IconSave size={16} /> Save QA Review</button>
        </form>
        <Table rows={qaReviews.map(q => [q.id, q.batchNumber, q.reviewType, q.decision, q.reviewDate, q.comments || ''])} />
      </>}

      {tab === 'Reconciliation' && <>
        <h2>Batch Reconciliation</h2>
        <form onSubmit={saveReconciliation} className="form">
          <SF label="Batch*" error={recfErrors.batchId}>
            <select value={recf.batchId} onChange={e => setRecf({ ...recf, batchId: e.target.value })}>
              <option value="">Select</option>{batches.map(b => <option key={b.id} value={b.id}>{b.batchNumber}</option>)}
            </select>
          </SF>
          <TF label="Starting Quantity*" type="number" step="0.001" min="0" value={recf.startingQuantity} error={recfErrors.startingQuantity} onChange={v => recfChange('startingQuantity')(v)} />
          <TF label="Good Quantity" type="number" step="0.001" min="0" value={recf.goodQuantity} error={recfErrors.goodQuantity} onChange={v => recfChange('goodQuantity')(v)} />
          <TF label="Reject Quantity" type="number" step="0.001" min="0" value={recf.rejectQuantity} error={recfErrors.rejectQuantity} onChange={v => recfChange('rejectQuantity')(v)} />
          <TF label="Unused Quantity" type="number" step="0.001" min="0" value={recf.unusedQuantity} error={recfErrors.unusedQuantity} onChange={v => recfChange('unusedQuantity')(v)} />
          <TF label="Calculated By (user id)" type="number" min="1" value={recf.calculatedBy} error={recfErrors.calculatedBy} onChange={v => recfChange('calculatedBy')(v)} />
          <TF label="Remarks" value={recf.remarks} error={recfErrors.remarks} onChange={v => recfChange('remarks')(v)} />
          <button className="primary btn-icon"><IconSave size={16} /> Calculate Reconciliation</button>
        </form>
        <Table rows={reconciliations.map(r => [r.id, r.batchNumber, r.startingQuantity, r.reconciledQuantity, r.variance, r.status])} />
      </>}

      {tab === 'CAPA' && <>
        <h2>CAPA Actions</h2>
        <form onSubmit={saveCapa} className="form">
          <SF label="Deviation*" error={capafErrors.deviationId}>
            <select value={capaf.deviationId} onChange={e => setCapaf({ ...capaf, deviationId: e.target.value })}>
              <option value="">Select</option>{deviations.map(d => <option key={d.id} value={d.id}>{d.deviationNumber} - {d.title}</option>)}
            </select>
          </SF>
          <SF label="Action Type*">
            <select value={capaf.actionType} onChange={e => setCapaf({ ...capaf, actionType: e.target.value })}>
              <option value="CORRECTIVE">CORRECTIVE</option><option value="PREVENTIVE">PREVENTIVE</option>
            </select>
          </SF>
          <TF label="Due Date" type="date" value={capaf.dueDate} error={capafErrors.dueDate} onChange={v => capafChange('dueDate')(v)} />
          <TF label="Owner (user id)" type="number" min="1" value={capaf.ownerId} error={capafErrors.ownerId} onChange={v => capafChange('ownerId')(v)} />
          <TF label="Action Description*" value={capaf.actionDescription} error={capafErrors.actionDescription} onChange={v => capafChange('actionDescription')(v)} />
          <button className="primary btn-icon"><IconSave size={16} /> Log CAPA Action</button>
        </form>
        <Table rows={capaActions.map(c => [c.id, c.deviationNumber || '', c.actionType, c.status, c.dueDate || '', c.actionDescription])} />
      </>}

      {tab === 'AQL' && <>
        <h2>AQL Inspection</h2>
        <form onSubmit={saveAql} className="form">
          <SF label="Batch*" error={aqlfErrors.batchId}>
            <select value={aqlf.batchId} onChange={e => setAqlf({ ...aqlf, batchId: e.target.value })}>
              <option value="">Select</option>{batches.map(b => <option key={b.id} value={b.id}>{b.batchNumber}</option>)}
            </select>
          </SF>
          <SF label="AQL Plan*" error={aqlfErrors.planId}>
            <select value={aqlf.planId} onChange={e => setAqlf({ ...aqlf, planId: e.target.value })}>
              <option value="">Select</option>{aqlPlans.map(p => <option key={p.id} value={p.id}>{p.planCode} (AQL {p.aqlValue})</option>)}
            </select>
          </SF>
          <TF label="Sample Size" type="number" min="1" value={aqlf.sampleSize} error={aqlfErrors.sampleSize} onChange={v => aqlfChange('sampleSize')(v)} placeholder="defaults to plan's sample size" />
          <TF label="Defects Found*" type="number" min="0" value={aqlf.defectsFound} error={aqlfErrors.defectsFound} onChange={v => aqlfChange('defectsFound')(v)} />
          <TF label="Inspector (user id)" type="number" min="1" value={aqlf.inspectorId} error={aqlfErrors.inspectorId} onChange={v => aqlfChange('inspectorId')(v)} />
          <TF label="Remarks" value={aqlf.remarks} error={aqlfErrors.remarks} onChange={v => aqlfChange('remarks')(v)} />
          <button className="primary btn-icon"><IconSave size={16} /> Record Inspection</button>
        </form>
        <h3>Inspections</h3>
        <Table rows={aqlInspections.map(a => [a.id, a.batchNumber, a.planCode, a.sampleSize, a.defectsFound, a.result])} />
        <h3>AQL Plans (reference)</h3>
        <Table rows={aqlPlans.map(p => [p.id, p.planCode, p.inspectionLevel, p.aqlValue, p.sampleSize, p.acceptanceNumber, p.rejectionNumber])} />
      </>}

      {tab === 'Production' && <>
        <h2>Production Run</h2>
        <form onSubmit={saveRun} className="form">
          <SF label="Batch*" error={runfErrors.batchId}>
            <select value={runf.batchId} onChange={e => setRunf({ ...runf, batchId: e.target.value })}>
              <option value="">Select</option>{batches.map(b => <option key={b.id} value={b.id}>{b.batchNumber}</option>)}
            </select>
          </SF>
          <SF label="Line*" error={runfErrors.lineId}>
            <select value={runf.lineId} onChange={e => setRunf({ ...runf, lineId: e.target.value })}>
              <option value="">Select</option>{lines.map(l => <option key={l.id} value={l.id}>{l.lineCode} - {l.lineName}</option>)}
            </select>
          </SF>
          <SF label="Equipment">
            <select value={runf.equipmentId} onChange={e => setRunf({ ...runf, equipmentId: e.target.value })}>
              <option value="">(none)</option>{equipmentList.map(eq => <option key={eq.id} value={eq.id}>{eq.equipmentCode} - {eq.equipmentName}</option>)}
            </select>
          </SF>
          <button className="primary btn-icon"><IconSave size={16} /> Start Production Run</button>
        </form>
        <Table rows={productionRuns.map(r => [r.id, r.batchNumber || '', r.lineCode || '', r.equipmentCode || '', r.status, r.startedAt || ''])} />

        <h2>Production Entry</h2>
        <form onSubmit={saveEntry} className="form">
          <SF label="Production Run*" error={entryfErrors.runId}>
            <select value={entryf.runId} onChange={e => setEntryf({ ...entryf, runId: e.target.value })}>
              <option value="">Select</option>{productionRuns.map(r => <option key={r.id} value={r.id}>#{r.id} - {r.batchNumber} ({r.status})</option>)}
            </select>
          </SF>
          <TF label="Produced*" type="number" step="0.001" min="0" value={entryf.produced} error={entryfErrors.produced} onChange={v => entryfChange('produced')(v)} />
          <TF label="Good" type="number" step="0.001" min="0" value={entryf.good} error={entryfErrors.good} onChange={v => entryfChange('good')(v)} />
          <TF label="Reject" type="number" step="0.001" min="0" value={entryf.reject} error={entryfErrors.reject} onChange={v => entryfChange('reject')(v)} />
          <TF label="Operator (user id)" type="number" min="1" value={entryf.operatorId} error={entryfErrors.operatorId} onChange={v => entryfChange('operatorId')(v)} />
          <TF label="Remarks" value={entryf.remarks} error={entryfErrors.remarks} onChange={v => entryfChange('remarks')(v)} />
          <button className="primary btn-icon"><IconSave size={16} /> Log Entry</button>
        </form>

        <h3>Entries by Run</h3>
        <div className="form" style={{ gridTemplateColumns: '1fr' }}>
          <label>Choose a run to view its entries<select value={entriesRunId} onChange={e => loadEntries(e.target.value)}>
            <option value="">Select</option>{productionRuns.map(r => <option key={r.id} value={r.id}>#{r.id} - {r.batchNumber} ({r.status})</option>)}
          </select></label>
        </div>
        <Table rows={entries.map(e => [e.id, e.entryTime, e.quantityProduced, e.quantityGood, e.quantityReject, e.remarks || ''])} />
      </>}

      {tab === 'Serialization' && <>
        <h2>Packaging Line / L3 Serialization Manager</h2>
        <div className="dashboard-grid">
          <div className="card"><strong>PLC</strong><div className="status" data-testid="plc-state">{lineStatus.plcState}</div></div>
          <div className="card"><strong>Printer</strong><div className="status">{lineStatus.devices['PRINTER-01'] || 'READY'}</div></div>
          <div className="card"><strong>Vision</strong><div className="status">{lineStatus.devices['VISION-01'] || 'READY'}</div></div>
          <div className="card"><strong>Scanner</strong><div className="status">{lineStatus.devices['SCANNER-01'] || 'READY'}</div></div>
        </div>
        <div className="toolbar">
          <button data-testid="line-start" className="primary" onClick={() => lineAction('start')}>START LINE</button>
          <button data-testid="line-stop" onClick={() => lineAction('stop')}>STOP</button>
          <button onClick={() => lineAction('fault')}>SIMULATE FAULT</button>
          <button onClick={() => lineAction('emergency-stop')}>E-STOP</button>
          <button onClick={loadLineStatus}>REFRESH STATUS</button>
        </div>

        <h2>Commission Serial Numbers</h2>
        <form onSubmit={commissionSerials} className="form">
          <SF label="Batch*" error={serfErrors.batchId}>
            <select data-testid="serial-batch" value={serf.batchId} onChange={e => setSerf({ ...serf, batchId: e.target.value })}>
              <option value="">Select</option>{batches.map(b => <option key={b.id} value={b.id}>{b.batchNumber}</option>)}
            </select>
          </SF>
          <SF label="Aggregation Level*">
            <select value={serf.aggregationLevel} onChange={e => setSerf({ ...serf, aggregationLevel: e.target.value })}>
              <option value="UNIT">UNIT</option><option value="CASE">CASE</option><option value="PALLET">PALLET</option>
            </select>
          </SF>
          <TF label="Quantity*" testId="serial-quantity" type="number" min="1" value={serf.quantity} error={serfErrors.quantity} onChange={v => serfChange('quantity')(v)} />
          <TF label="GTIN" value={serf.gtin} error={serfErrors.gtin} onChange={v => serfChange('gtin')(v)} placeholder="optional GS1 GTIN" />
          <button data-testid="serial-generate" className="primary btn-icon"><IconSave size={16} /> Commission</button>
        </form>

        <h2>Vision / Cognex Simulator</h2>
        <form onSubmit={verifyVision} className="form">
          <TF label="Serial Number*" value={visionf.serialNumber} onChange={v => setVisionf({...visionf, serialNumber:v})} />
          <TF label="Actual DataMatrix*" value={visionf.actualBarcode} onChange={v => setVisionf({...visionf, actualBarcode:v})} />
          <TF label="Actual Lot*" value={visionf.actualLot} onChange={v => setVisionf({...visionf, actualLot:v})} />
          <TF label="Actual Expiry*" type="date" value={visionf.actualExpiry} onChange={v => setVisionf({...visionf, actualExpiry:v})} />
          <button className="primary btn-icon">Run Vision Verification</button>
        </form>

        <h2>Controlled Decommission / Exception Workflow</h2>
        <form onSubmit={decommissionSerial} className="form">
          <TF label="Serial Number*" value={decomf.serialNumber} onChange={v => setDecomf({...decomf, serialNumber:v})} />
          <SF label="Reason">
            <select value={decomf.reason} onChange={e => setDecomf({...decomf, reason:e.target.value})}>
              <option>DAMAGED_PACKAGE</option><option>FAILED_VISION</option><option>DUPLICATE_SERIAL</option>
              <option>WRONG_PRODUCT</option><option>WRONG_LOT</option><option>WRONG_EXPIRY</option>
              <option>REWORK_FAILED</option><option>QUALITY_HOLD</option><option>OTHER</option>
            </select>
          </SF>
          <TF label="Comment" value={decomf.comment} onChange={v => setDecomf({...decomf, comment:v})} />
          <button className="primary btn-icon">Decommission</button>
        </form>

        <h2>Aggregate Units</h2>
        <form onSubmit={aggregateSerials} className="form">
          <TF label="Parent Serial Number*" value={aggf.parentSerialNumber} error={aggfErrors.parentSerialNumber} onChange={v => aggfChange('parentSerialNumber')(v)} placeholder="e.g. SN-BATCH-DEMO-001-CASE-000001" />
          <TF label="Child Serial Numbers* (comma-separated)" value={aggf.childSerialNumbers} error={aggfErrors.childSerialNumbers} onChange={v => setAggf({ ...aggf, childSerialNumbers: v })} placeholder="SN-...-000001, SN-...-000002" />
          <button className="primary btn-icon"><IconSave size={16} /> Aggregate</button>
        </form>

        <h3>Serialized Units</h3>
        <div className="form" style={{ gridTemplateColumns: '1fr' }}>
          <label>Choose a batch to view its serialized units<select data-testid="units-batch" value={serialBatchId} onChange={e => loadSerialization(e.target.value)}>
            <option value="">Select</option>{batches.map(b => <option key={b.id} value={b.id}>{b.batchNumber}</option>)}
          </select></label>
        </div>
        <table><thead><tr><th>ID</th><th>Serial</th><th>Level</th><th>Parent</th><th>Status</th><th>Commissioned</th><th>Actions</th></tr></thead>
          <tbody>{serializedUnits.map(u => <tr key={u.id} data-testid="unit-row" data-serial={u.serialNumber} data-status={u.status}>
            <td>{u.id}</td><td>{u.serialNumber}</td><td>{u.aggregationLevel}</td><td>{u.parentSerialNumber || ''}</td><td>{u.status}</td><td>{u.commissionedAt || ''}</td>
            <td><button data-testid="unit-print" onClick={() => printSerial(u.serialNumber)} disabled={u.status !== 'CREATED'}>Print</button>{' '}
                <button data-testid="unit-commission" onClick={() => commissionSerial(u.serialNumber)} disabled={u.status !== 'VISION_VERIFIED'}>Commission</button></td>
          </tr>)}</tbody></table>
      </>}

      {tab === 'Audit Trail' && <>
        <h2>Audit Trail</h2>
        <p>System-recorded who / what / when for every tracked GMP-critical change (batch creation and status changes, deviations, CAPA actions, QA review decisions, reconciliation calculations) — newest first.</p>
        <Table rows={auditTrail.map(a => [a.id, a.createdAt, a.username, a.actionType, a.entityName || '', a.entityId || '', a.fieldName || '', a.oldValue || '', a.newValue || ''])} />
      </>}

      {tab === 'E-Signatures' && <>
        <h2>Electronic Signatures</h2>
        <form onSubmit={saveSignature} className="form">
          <SF label="Record Type*">
            <select value={sigf.entityName} onChange={e => setSigf({ ...sigf, entityName: e.target.value })}>
              <option value="Batch">Batch</option><option value="Deviation">Deviation</option><option value="CapaAction">CapaAction</option>
              <option value="QAReview">QAReview</option><option value="Reconciliation">Reconciliation</option>
            </select>
          </SF>
          <TF label="Record ID*" value={sigf.entityId} error={sigfErrors.entityId} onChange={v => sigfChange('entityId')(v)} placeholder="the record's numeric id" />
          <SF label="Action*">
            <select value={sigf.actionType} onChange={e => setSigf({ ...sigf, actionType: e.target.value })}>
              <option value="APPROVE">APPROVE</option><option value="REJECT">REJECT</option><option value="RELEASE">RELEASE</option>
              <option value="CLOSE">CLOSE</option><option value="REVIEW">REVIEW</option>
            </select>
          </SF>
          <TF label="Signature Reason" value={sigf.signatureReason} error={sigfErrors.signatureReason} onChange={v => sigfChange('signatureReason')(v)} placeholder="meaning of this signature" />
          <button className="primary btn-icon"><IconSave size={16} /> Sign as {auth.fullName}</button>
        </form>
        <p className="notice">Signed as the currently logged-in user ({auth.username}) — the signer identity comes from your session token and cannot be typed in.</p>
        <Table rows={signatures.map(s => [s.id, s.signedAt, s.fullName, s.entityName, s.entityId, s.actionType, s.signatureReason || ''])} />
      </>}
    </main>
  </div>;
}

createRoot(document.getElementById('root')!).render(<App />);
