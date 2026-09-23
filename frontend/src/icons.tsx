// Self-contained inline-SVG icon set - no external icon font or network fetch needed,
// so the app stays a single deployable bundle (consistent with the rest of this project).
import React from 'react';

type IconProps = { size?: number };
const base = (size = 18) => ({ width: size, height: size, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', strokeWidth: 2, strokeLinecap: 'round' as const, strokeLinejoin: 'round' as const });

export const IconHome = ({ size }: IconProps) => <svg {...base(size)}><path d="M3 11.5 12 4l9 7.5" /><path d="M5 10v9a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1v-9" /></svg>;
export const IconDashboard = ({ size }: IconProps) => <svg {...base(size)}><rect x="3" y="3" width="7" height="9" rx="1.5" /><rect x="14" y="3" width="7" height="5" rx="1.5" /><rect x="14" y="12" width="7" height="9" rx="1.5" /><rect x="3" y="16" width="7" height="5" rx="1.5" /></svg>;
export const IconProduct = ({ size }: IconProps) => <svg {...base(size)}><path d="M21 8 12 3 3 8l9 5 9-5Z" /><path d="M3 8v8l9 5 9-5V8" /><path d="M12 13v8" /></svg>;
export const IconBatch = ({ size }: IconProps) => <svg {...base(size)}><rect x="4" y="7" width="16" height="13" rx="1.5" /><path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /><path d="M4 12h16" /></svg>;
export const IconProduction = ({ size }: IconProps) => <svg {...base(size)}><circle cx="8" cy="16" r="3" /><circle cx="17" cy="16" r="3" /><path d="M8 13V6a1 1 0 0 1 1-1h3l4 5h3a2 2 0 0 1 2 2v4" /></svg>;
export const IconAql = ({ size }: IconProps) => <svg {...base(size)}><circle cx="11" cy="11" r="7" /><path d="m21 21-4.3-4.3" /><path d="m9 11 1.5 1.5L14 9" /></svg>;
export const IconReconciliation = ({ size }: IconProps) => <svg {...base(size)}><path d="M7 7h11l-3-3" /><path d="M17 17H6l3 3" /></svg>;
export const IconQa = ({ size }: IconProps) => <svg {...base(size)}><path d="M12 3 4 6v6c0 5 3.5 8 8 9 4.5-1 8-4 8-9V6l-8-3Z" /><path d="m9 12 2 2 4-4" /></svg>;
export const IconDeviation = ({ size }: IconProps) => <svg {...base(size)}><path d="M10.3 3.8 2.6 18a1 1 0 0 0 .9 1.5h17a1 1 0 0 0 .9-1.5L13.7 3.8a1 1 0 0 0-1.74 0Z" /><path d="M12 9v4" /><path d="M12 16.5h.01" /></svg>;
export const IconCapa = ({ size }: IconProps) => <svg {...base(size)}><path d="M12 20a8 8 0 1 0-8-8" /><path d="M4 4v5h5" /><path d="M12 8v4l3 2" /></svg>;
export const IconSave = ({ size }: IconProps) => <svg {...base(size)}><path d="M5 4h11l3 3v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1Z" /><path d="M8 4v5h8V4" /><path d="M8 20v-6h8v6" /></svg>;
export const IconUser = ({ size }: IconProps) => <svg {...base(size)}><circle cx="12" cy="8" r="4" /><path d="M4 20c1.5-4 4.5-6 8-6s6.5 2 8 6" /></svg>;
export const IconLock = ({ size }: IconProps) => <svg {...base(size)}><rect x="5" y="11" width="14" height="9" rx="1.5" /><path d="M8 11V8a4 4 0 0 1 8 0v3" /></svg>;
export const IconLogout = ({ size }: IconProps) => <svg {...base(size)}><path d="M9 4H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h3" /><path d="M15 16l4-4-4-4" /><path d="M19 12H9" /></svg>;
export const IconHelp = ({ size }: IconProps) => <svg {...base(size)}><circle cx="12" cy="12" r="9" /><path d="M9.3 9a2.7 2.7 0 0 1 5.2.9c0 1.8-2.5 1.6-2.5 3.6" /><path d="M12 17h.01" /></svg>;
export const IconGlobe = ({ size }: IconProps) => <svg {...base(size)}><circle cx="12" cy="12" r="9" /><path d="M3 12h18" /><path d="M12 3a15 15 0 0 1 0 18a15 15 0 0 1 0-18Z" /></svg>;

// Phase 3D icons — Serialization (a barcode), Audit Trail (a clock/history), Electronic
// Signature (a signing pen over a line), each following the same inline-SVG pattern above.
export const IconSerial = ({ size }: IconProps) => <svg {...base(size)}><path d="M4 4v16" /><path d="M8 4v16" /><path d="M11 4v16" /><path d="M13.5 4v16" /><path d="M17 4v16" /><path d="M20 4v16" /></svg>;
export const IconAudit = ({ size }: IconProps) => <svg {...base(size)}><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3.5 2" /></svg>;
export const IconSignature = ({ size }: IconProps) => <svg {...base(size)}><path d="M3 17c2-4 3-4 4 0s2 4 4 0 2-4 4 0 2-3.5 4-1" /><path d="M4 21h16" /></svg>;
