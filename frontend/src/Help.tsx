import React, { useState } from 'react';
import { IconGlobe, IconHelp } from './icons';

// Phase 3A shipped the multilingual help *mechanism* (button, language switcher, popover)
// wired into every screen. Phase 3C (this file) fills in real, screen-specific help text
// for all 9 modules across all 6 languages: English, বাংলা (Bangla), हिन्दी (Hindi),
// Español (Spanish), Français (French) and 中文 (Chinese). FALLBACK below is now only a
// safety net for a topic key that doesn't match one of the 9 tabs.
export const LANGS = [
  { code: 'en', label: 'English' },
  { code: 'bn', label: 'বাংলা' },
  { code: 'hi', label: 'हिन्दी' },
  { code: 'es', label: 'Español' },
  { code: 'fr', label: 'Français' },
  { code: 'zh', label: '中文' },
] as const;
export type LangCode = typeof LANGS[number]['code'];

type HelpText = Partial<Record<LangCode, string>>;

const HELP: Record<string, HelpText> = {
  Dashboard: {
    en: 'Overview of products, batches and open deviations. Use the tabs above to navigate each module of the packaging QMS.',
    bn: 'প্রোডাক্ট, ব্যাচ এবং খোলা ডেভিয়েশনের সারসংক্ষেপ। উপরের ট্যাব ব্যবহার করে QMS-এর প্রতিটা মডিউলে যাও।',
    hi: 'उत्पादों, बैचों और खुले डिविएशन (deviations) का सारांश। पैकेजिंग QMS के हर मॉड्यूल में जाने के लिए ऊपर दिए गए टैब का उपयोग करें।',
    es: 'Resumen de productos, lotes y desviaciones abiertas. Usa las pestañas de arriba para navegar por cada módulo del sistema de gestión de calidad (QMS) de empaquetado.',
    fr: "Aperçu des produits, des lots et des écarts (déviations) ouverts. Utilisez les onglets ci-dessus pour naviguer dans chaque module du système de gestion de la qualité (QMS) de conditionnement.",
    zh: '产品、批次和未结偏差（deviation）的概览。使用上方的选项卡浏览包装QMS（质量管理系统）的各个模块。',
  },
  Products: {
    en: 'Create and manage the product master list. Enter a unique Product Code and Product Name (required), plus optional Strength, Dosage Form (choose from the dropdown) and Pack Size. The code is automatically capitalized; the name allows letters, numbers, spaces and a few common punctuation marks — no other special characters.',
    bn: 'প্রোডাক্ট মাস্টার লিস্ট তৈরি ও পরিচালনা করো। একটি ইউনিক Product Code এবং Product Name (আবশ্যক) দাও, সাথে ঐচ্ছিক Strength, Dosage Form (ড্রপডাউন থেকে বেছে নাও) এবং Pack Size। Code স্বয়ংক্রিয়ভাবে বড় হাতের অক্ষরে চলে যায়; Name-এ অক্ষর, সংখ্যা, স্পেস আর কিছু সাধারণ চিহ্ন ছাড়া অন্য কোনো স্পেশাল ক্যারেক্টার দেওয়া যাবে না।',
    hi: 'प्रोडक्ट मास्टर लिस्ट बनाएं और प्रबंधित करें। एक यूनीक Product Code और Product Name (आवश्यक) दर्ज करें, साथ ही वैकल्पिक Strength, Dosage Form (ड्रॉपडाउन से चुनें) और Pack Size। Code अपने आप बड़े अक्षरों (uppercase) में बदल जाता है; Name में अक्षर, अंक, स्पेस और कुछ सामान्य चिह्नों के अलावा कोई विशेष चिह्न मान्य नहीं है।',
    es: 'Crea y gestiona la lista maestra de productos. Introduce un Código de Producto y un Nombre de Producto únicos (obligatorios), además de la Concentración/Fuerza, la Forma Farmacéutica (elige en el menú desplegable) y el Tamaño del Envase, que son opcionales. El código se pone en mayúsculas automáticamente; el nombre admite letras, números, espacios y algunos signos de puntuación comunes, pero ningún otro carácter especial.',
    fr: "Créez et gérez la liste maîtresse des produits. Saisissez un Code Produit et un Nom de Produit uniques (obligatoires), ainsi que le Dosage, la Forme Galénique (à choisir dans la liste déroulante) et la Taille du Conditionnement, qui sont facultatifs. Le code passe automatiquement en majuscules ; le nom accepte les lettres, les chiffres, les espaces et quelques signes de ponctuation courants, mais aucun autre caractère spécial.",
    zh: '创建并管理产品主数据列表。请输入唯一的产品编号（Product Code）和产品名称（Product Name，必填），以及可选的规格（Strength）、剂型（Dosage Form，从下拉菜单选择）和包装规格（Pack Size）。产品编号会自动转为大写；产品名称只能包含字母、数字、空格和少数常见标点符号，不允许使用其他特殊字符。',
  },
  Batches: {
    en: 'Create a manufacturing batch against an existing product. Batch Number and Product are required; Lot Size must be a positive decimal quantity. Manufacturing Date and Expiry Date are optional, but if both are given, Expiry Date cannot be earlier than the Manufacturing Date.',
    bn: 'একটি বিদ্যমান প্রোডাক্টের বিপরীতে ম্যানুফ্যাকচারিং ব্যাচ তৈরি করো। Batch Number এবং Product আবশ্যক; Lot Size অবশ্যই একটি পজিটিভ decimal সংখ্যা হতে হবে। Manufacturing Date এবং Expiry Date ঐচ্ছিক, তবে দুটোই দিলে Expiry Date কখনো Manufacturing Date-এর আগে হতে পারবে না।',
    hi: 'किसी मौजूदा प्रोडक्ट के लिए मैन्युफैक्चरिंग बैच बनाएं। Batch Number और Product आवश्यक हैं; Lot Size एक धनात्मक (positive) दशमलव मात्रा होनी चाहिए। Manufacturing Date और Expiry Date वैकल्पिक हैं, लेकिन यदि दोनों दी जाएं तो Expiry Date, Manufacturing Date से पहले नहीं हो सकती।',
    es: 'Crea un lote de fabricación para un producto existente. El Número de Lote y el Producto son obligatorios; el Tamaño del Lote debe ser una cantidad decimal positiva. La Fecha de Fabricación y la Fecha de Caducidad son opcionales, pero si se indican ambas, la Fecha de Caducidad no puede ser anterior a la Fecha de Fabricación.',
    fr: "Créez un lot de fabrication pour un produit existant. Le Numéro de Lot et le Produit sont obligatoires ; la Taille du Lot doit être une quantité décimale positive. La Date de Fabrication et la Date de Péremption sont facultatives, mais si les deux sont renseignées, la Date de Péremption ne peut pas être antérieure à la Date de Fabrication.",
    zh: '为现有产品创建生产批次。批号（Batch Number）和产品（Product）为必填项；批量（Lot Size）必须是正的小数数量。生产日期（Manufacturing Date）和有效期（Expiry Date）为选填项，但若两者都填写，有效期不能早于生产日期。',
  },
  Production: {
    en: 'Start a Production Run by selecting a Batch and a packaging Line (Equipment is optional). Once a run exists, log Production Entries against it below — Produced, Good and Reject quantities. Good plus Reject cannot exceed the Produced quantity for that entry.',
    bn: 'একটি Batch এবং packaging Line বেছে নিয়ে Production Run শুরু করো (Equipment ঐচ্ছিক)। Run তৈরি হলে, নিচের ফর্মে সেটার বিপরীতে Production Entry লগ করো — Produced, Good এবং Reject পরিমাণ। প্রতিটা এন্ট্রিতে Good + Reject কখনো Produced-এর বেশি হতে পারবে না।',
    hi: 'एक Batch और पैकेजिंग Line चुनकर Production Run शुरू करें (Equipment वैकल्पिक है)। Run बनने के बाद, नीचे दिए फॉर्म में उसके लिए Production Entry दर्ज करें — Produced, Good और Reject मात्राएं। हर एंट्री में Good + Reject, Produced मात्रा से अधिक नहीं हो सकता।',
    es: 'Inicia una Corrida de Producción seleccionando un Lote y una Línea de empaquetado (el Equipo es opcional). Una vez creada la corrida, registra los Registros de Producción correspondientes más abajo: cantidades Producida, Buena y Rechazada. La suma de Buena y Rechazada no puede superar la cantidad Producida en ese registro.',
    fr: "Démarrez un Ordre de Fabrication en sélectionnant un Lot et une Ligne de conditionnement (l'Équipement est facultatif). Une fois l'ordre créé, enregistrez les Saisies de Production correspondantes ci-dessous : quantités Produite, Bonne et Rejetée. La somme de Bonne et Rejetée ne peut pas dépasser la quantité Produite pour cette saisie.",
    zh: '选择一个批次（Batch）和包装产线（Line）以启动生产运行（Production Run，设备为选填项）。生产运行创建后，可在下方表单中为其登记生产记录（Production Entry）——生产量（Produced）、合格量（Good）和不合格量（Reject）。每条记录中，合格量加不合格量不能超过生产量。',
  },
  AQL: {
    en: "Record an Acceptable Quality Level (AQL) inspection for a batch against a sampling Plan. Sample Size defaults to the plan's own sample size if left blank; Defects Found is required and must be zero or more. The AQL Plans table below is reference data (inspection level, sample size, acceptance/rejection numbers).",
    bn: 'একটি sampling Plan অনুযায়ী কোনো Batch-এর Acceptable Quality Level (AQL) ইন্সপেকশন রেকর্ড করো। Sample Size ফাঁকা রাখলে সেটা প্ল্যানের নিজস্ব sample size ব্যবহার করবে; Defects Found আবশ্যক এবং শূন্য বা তার বেশি হতে হবে। নিচের AQL Plans টেবিল হলো রেফারেন্স ডেটা (inspection level, sample size, acceptance/rejection numbers)।',
    hi: 'किसी sampling Plan के अनुसार किसी Batch के लिए Acceptable Quality Level (AQL) निरीक्षण दर्ज करें। यदि Sample Size खाली छोड़ा जाए, तो वह प्लान के अपने सैंपल साइज़ का उपयोग करता है; Defects Found आवश्यक है और शून्य या उससे अधिक होना चाहिए। नीचे दी गई AQL Plans टेबल संदर्भ डेटा है (inspection level, sample size, acceptance/rejection numbers)।',
    es: 'Registra una inspección de Nivel de Calidad Aceptable (AQL) para un lote según un Plan de muestreo. El Tamaño de Muestra usa por defecto el del propio plan si se deja en blanco; los Defectos Encontrados son obligatorios y deben ser cero o más. La tabla de Planes AQL de abajo es información de referencia (nivel de inspección, tamaño de muestra, números de aceptación/rechazo).',
    fr: "Enregistrez une inspection de Niveau de Qualité Acceptable (AQL) pour un lot, selon un Plan d'échantillonnage. La Taille d'Échantillon reprend par défaut celle du plan si elle est laissée vide ; le nombre de Défauts Constatés est obligatoire et doit être égal ou supérieur à zéro. Le tableau des Plans AQL ci-dessous est une donnée de référence (niveau d'inspection, taille d'échantillon, nombres d'acceptation/de rejet).",
    zh: '根据抽样方案（Plan）为某批次登记可接受质量水平（AQL）检验。若样本量（Sample Size）留空，则默认使用该方案自身的样本量；发现缺陷数（Defects Found）为必填项，且必须大于或等于零。下方的AQL方案表为参考数据（检验水平、样本量、允收/拒收数）。',
  },
  Reconciliation: {
    en: 'Calculate material reconciliation for a batch: enter the Starting Quantity plus the Good, Reject and Unused quantities. The three output quantities together cannot exceed the Starting Quantity — the system calculates the Reconciled Quantity and Variance automatically.',
    bn: 'একটি Batch-এর জন্য material reconciliation হিসাব করো: Starting Quantity এবং Good, Reject ও Unused quantity দাও। এই তিনটা আউটপুট quantity একসাথে Starting Quantity-এর বেশি হতে পারবে না — সিস্টেম স্বয়ংক্রিয়ভাবে Reconciled Quantity এবং Variance হিসাব করে দেবে।',
    hi: 'किसी Batch के लिए material reconciliation की गणना करें: Starting Quantity के साथ Good, Reject और Unused मात्राएं दर्ज करें। ये तीनों आउटपुट मात्राएं मिलाकर Starting Quantity से अधिक नहीं हो सकतीं — सिस्टम स्वतः Reconciled Quantity और Variance की गणना कर देता है।',
    es: 'Calcula la reconciliación de materiales de un lote: introduce la Cantidad Inicial junto con las cantidades Buena, Rechazada y No Utilizada. Estas tres cantidades de salida juntas no pueden superar la Cantidad Inicial; el sistema calcula automáticamente la Cantidad Reconciliada y la Varianza.',
    fr: "Calculez la réconciliation matière d'un lot : saisissez la Quantité de Départ ainsi que les quantités Bonne, Rejetée et Non Utilisée. Ces trois quantités de sortie, additionnées, ne peuvent pas dépasser la Quantité de Départ — le système calcule automatiquement la Quantité Réconciliée et l'Écart (variance).",
    zh: '计算某批次的物料平衡（reconciliation）：输入起始量（Starting Quantity），以及合格量（Good）、不合格量（Reject）和未使用量（Unused）。这三项输出数量之和不能超过起始量——系统会自动计算平衡数量（Reconciled Quantity）和差异（Variance）。',
  },
  'QA Review': {
    en: 'Log a Quality Assurance review for a batch: choose the Batch, Review Type (IPQC / FINAL / LINE_CLEARANCE) and Decision (PASS / FAIL / PENDING), with optional Reviewer and Comments.',
    bn: 'একটি Batch-এর জন্য Quality Assurance review লগ করো: Batch, Review Type (IPQC / FINAL / LINE_CLEARANCE) এবং Decision (PASS / FAIL / PENDING) বেছে নাও, সাথে ঐচ্ছিক Reviewer এবং Comments।',
    hi: 'किसी Batch के लिए Quality Assurance review दर्ज करें: Batch, Review Type (IPQC / FINAL / LINE_CLEARANCE) और Decision (PASS / FAIL / PENDING) चुनें, साथ में वैकल्पिक Reviewer और Comments।',
    es: 'Registra una revisión de Aseguramiento de Calidad (QA) para un lote: elige el Lote, el Tipo de Revisión (IPQC / FINAL / LINE_CLEARANCE) y la Decisión (PASS / FAIL / PENDING), con Revisor y Comentarios opcionales.',
    fr: "Enregistrez une revue d'Assurance Qualité (QA) pour un lot : choisissez le Lot, le Type de Revue (IPQC / FINAL / LINE_CLEARANCE) et la Décision (PASS / FAIL / PENDING), avec un Réviseur et des Commentaires facultatifs.",
    zh: '为某批次登记质量保证（QA）审核：选择批次、审核类型（Review Type：IPQC / FINAL / LINE_CLEARANCE）和审核结论（Decision：PASS / FAIL / PENDING），审核人（Reviewer）和备注（Comments）为选填项。',
  },
  Deviations: {
    en: 'Log a quality deviation. Deviation Number and Title are required and auto-capitalized; Description is required. Severity (MINOR / MAJOR / CRITICAL) controls how the deviation is tracked; link it to a Batch if relevant.',
    bn: 'একটি quality deviation লগ করো। Deviation Number এবং Title আবশ্যক এবং স্বয়ংক্রিয়ভাবে capitalize হয়; Description আবশ্যক। Severity (MINOR / MAJOR / CRITICAL) নির্ধারণ করে deviation-টা কীভাবে ট্র্যাক হবে; প্রাসঙ্গিক হলে একটা Batch-এর সাথে লিঙ্ক করো।',
    hi: 'एक quality deviation दर्ज करें। Deviation Number और Title आवश्यक हैं और स्वतः capitalize होते हैं; Description आवश्यक है। Severity (MINOR / MAJOR / CRITICAL) यह तय करती है कि deviation कैसे ट्रैक किया जाएगा; प्रासंगिक हो तो इसे किसी Batch से लिंक करें।',
    es: 'Registra una desviación de calidad. El Número de Desviación y el Título son obligatorios y se ponen en mayúscula automáticamente; la Descripción es obligatoria. La Gravedad (MINOR / MAJOR / CRITICAL) determina cómo se hace seguimiento de la desviación; enlázala a un Lote si corresponde.',
    fr: "Enregistrez un écart qualité (déviation). Le Numéro de Déviation et le Titre sont obligatoires et mis en majuscules automatiquement ; la Description est obligatoire. La Gravité (MINOR / MAJOR / CRITICAL) détermine la façon dont la déviation est suivie ; associez-la à un Lot si pertinent.",
    zh: '登记一项质量偏差（deviation）。偏差编号（Deviation Number）和标题（Title）为必填项，并会自动转为大写首字母；描述（Description）为必填项。严重程度（Severity：MINOR / MAJOR / CRITICAL）决定该偏差的跟踪方式；如相关，可关联到某个批次。',
  },
  CAPA: {
    en: 'Log a Corrective or Preventive Action (CAPA) against an existing Deviation. Choose the Deviation and Action Type, and describe the action to be taken. Due Date and Owner (user id) are optional but recommended for tracking.',
    bn: 'একটি বিদ্যমান Deviation-এর বিপরীতে Corrective বা Preventive Action (CAPA) লগ করো। Deviation এবং Action Type বেছে নাও, এবং কী action নেওয়া হবে তা বর্ণনা করো। Due Date এবং Owner (user id) ঐচ্ছিক তবে ট্র্যাকিংয়ের জন্য সুপারিশ করা হয়।',
    hi: 'किसी मौजूदा Deviation के लिए Corrective या Preventive Action (CAPA) दर्ज करें। Deviation और Action Type चुनें, और बताएं कि कौन-सी कार्रवाई की जाएगी। Due Date और Owner (user id) वैकल्पिक हैं लेकिन ट्रैकिंग के लिए अनुशंसित हैं।',
    es: 'Registra una Acción Correctiva o Preventiva (CAPA) para una Desviación existente. Elige la Desviación y el Tipo de Acción, y describe la acción a realizar. La Fecha Límite y el Responsable (id de usuario) son opcionales pero se recomiendan para el seguimiento.',
    fr: "Enregistrez une Action Corrective ou Préventive (CAPA) pour une Déviation existante. Choisissez la Déviation et le Type d'Action, puis décrivez l'action à mener. La Date d'Échéance et le Responsable (id utilisateur) sont facultatifs mais recommandés pour le suivi.",
    zh: '针对已有偏差（Deviation）登记纠正或预防措施（CAPA）。选择对应的偏差和措施类型（Action Type），并描述将要采取的行动。截止日期（Due Date）和负责人（Owner，用户ID）为选填项，但建议填写以便跟踪。',
  },
  Serialization: {
    en: "Commission unit-, case- or pallet-level serial numbers for a batch, then aggregate lower-level units under a higher-level parent (UNIT under CASE under PALLET) — the standard DSCSA/EU-FMD-style traceability hierarchy. Quantity must be a whole number from 1 to 100,000; GTIN is optional. To aggregate, enter the parent's serial number and a comma-separated list of the child serial numbers; a child can only be aggregated under a parent one or more levels above it.",
    bn: 'একটি Batch-এর জন্য unit-, case- বা pallet-level সিরিয়াল নম্বর কমিশন করো, তারপর নিচের লেভেলের ইউনিটগুলোকে উপরের লেভেলের parent-এর অধীনে aggregate করো (UNIT থাকে CASE-এর নিচে, CASE থাকে PALLET-এর নিচে) — এটাই standard DSCSA/EU-FMD-স্টাইল traceability hierarchy। Quantity অবশ্যই ১ থেকে ১,০০,০০০-এর মধ্যে একটা পূর্ণ সংখ্যা হতে হবে; GTIN ঐচ্ছিক। Aggregate করতে parent-এর serial number আর comma দিয়ে আলাদা করা child serial number-গুলো দাও; একটা child শুধু তার থেকে এক বা তার বেশি লেভেল উপরের parent-এর অধীনেই aggregate হতে পারবে।',
    hi: 'किसी Batch के लिए unit-, case- या pallet-level सीरियल नंबर कमीशन करें, फिर निचले स्तर की इकाइयों को ऊपरी स्तर के parent के अंतर्गत aggregate करें (UNIT, CASE के नीचे, CASE, PALLET के नीचे) — यही मानक DSCSA/EU-FMD-शैली की traceability hierarchy है। Quantity 1 से 100,000 के बीच एक पूर्ण संख्या होनी चाहिए; GTIN वैकल्पिक है। Aggregate करने के लिए parent का serial number और कॉमा से अलग किए गए child serial number दर्ज करें; कोई child केवल उससे एक या अधिक स्तर ऊपर के parent के अंतर्गत ही aggregate हो सकता है।',
    es: 'Comisiona números de serie a nivel de unidad, caja o palet para un lote, y luego agrega las unidades de nivel inferior bajo un elemento padre de nivel superior (UNIT bajo CASE, CASE bajo PALLET) — la jerarquía de trazabilidad estándar de estilo DSCSA/EU-FMD. La Cantidad debe ser un número entero entre 1 y 100.000; el GTIN es opcional. Para agregar, introduce el número de serie del padre y una lista separada por comas de los números de serie de los hijos; un hijo solo puede agregarse bajo un padre de un nivel o más por encima del suyo.',
    fr: "Commissionnez des numéros de série au niveau unité, caisse ou palette pour un lot, puis regroupez (agrégation) les unités de niveau inférieur sous un élément parent de niveau supérieur (UNIT sous CASE, CASE sous PALLET) — la hiérarchie de traçabilité standard de type DSCSA/EU-FMD. La Quantité doit être un nombre entier compris entre 1 et 100 000 ; le GTIN est facultatif. Pour agréger, saisissez le numéro de série du parent et une liste, séparée par des virgules, des numéros de série des enfants ; un enfant ne peut être agrégé que sous un parent situé un niveau ou plus au-dessus du sien.",
    zh: '为某批次登记单件（UNIT）、箱（CASE）或托盘（PALLET）级别的序列号，然后将较低层级的单元聚合（aggregate）到较高层级的父级之下（UNIT 位于 CASE 之下，CASE 位于 PALLET 之下）——这是标准的 DSCSA / EU-FMD 式可追溯层级结构。数量（Quantity）必须是 1 到 100,000 之间的整数；GTIN 为选填项。要进行聚合，请输入父级的序列号，以及用逗号分隔的子级序列号列表；子级只能聚合到比自己高一级或以上的父级之下。',
  },
  'Audit Trail': {
    en: 'A read-only, system-generated record of who changed what and when across the GMP-critical modules (Batch, Deviation, CAPA, QA Review, Reconciliation). Every row is written automatically by the backend when a record is created or its status changes — nothing here can be edited or deleted, matching CGMP electronic-record traceability expectations.',
    bn: 'GMP-এর জন্য গুরুত্বপূর্ণ মডিউলগুলোতে (Batch, Deviation, CAPA, QA Review, Reconciliation) কে, কী পরিবর্তন করলো এবং কখন — সেটার একটা read-only, সিস্টেম-জেনারেটেড রেকর্ড। যখনই একটা রেকর্ড তৈরি হয় বা তার status পরিবর্তন হয়, তখন backend স্বয়ংক্রিয়ভাবে এখানে একটা row লিখে দেয় — এখানে কিছু edit বা delete করা যায় না, যা CGMP electronic-record traceability-র প্রত্যাশার সাথে মেলে।',
    hi: 'GMP की दृष्टि से महत्वपूर्ण मॉड्यूल्स (Batch, Deviation, CAPA, QA Review, Reconciliation) में किसने, क्या बदला और कब — इसका एक read-only, सिस्टम-जनरेटेड रिकॉर्ड। जब भी कोई रिकॉर्ड बनाया जाता है या उसकी स्थिति (status) बदलती है, बैकएंड स्वतः यहां एक पंक्ति (row) दर्ज करता है — यहां कुछ भी edit या delete नहीं किया जा सकता, जो CGMP इलेक्ट्रॉनिक-रिकॉर्ड ट्रेसेबिलिटी की अपेक्षाओं से मेल खाता है।',
    es: 'Un registro de solo lectura, generado automáticamente por el sistema, de quién cambió qué y cuándo en los módulos críticos para GMP (Lote, Desviación, CAPA, Revisión de QA, Reconciliación). Cada fila la escribe automáticamente el backend cuando se crea un registro o cambia su estado; nada aquí se puede editar ni eliminar, conforme a lo que exige la trazabilidad de registros electrónicos CGMP.',
    fr: "Un registre en lecture seule, généré automatiquement par le système, indiquant qui a changé quoi et quand dans les modules critiques pour les BPF/GMP (Lot, Déviation, CAPA, Revue QA, Réconciliation). Chaque ligne est écrite automatiquement par le backend à la création d'un enregistrement ou au changement de son statut ; rien ici ne peut être modifié ni supprimé, conformément aux exigences de traçabilité des enregistrements électroniques CGMP.",
    zh: '一份只读的、由系统自动生成的记录，记录了在GMP关键模块（批次、偏差、CAPA、QA审核、物料平衡）中谁在何时更改了什么。每当创建一条记录或其状态发生变化时，后端都会自动写入一行——此处内容不可编辑或删除，符合CGMP电子记录可追溯性的要求。',
  },
  'E-Signatures': {
    en: "Capture a CGMP-style electronic signature against any record: choose the Record Type and enter its Record ID, choose the Action (APPROVE / REJECT / RELEASE / CLOSE / REVIEW) and optionally explain why. The signer is always you, the logged-in user — it is read from your session and can never be typed in, so a signature can't be recorded under someone else's name.",
    bn: 'যেকোনো রেকর্ডের বিপরীতে একটা CGMP-স্টাইল electronic signature ক্যাপচার করো: Record Type বেছে নিয়ে তার Record ID দাও, Action (APPROVE / REJECT / RELEASE / CLOSE / REVIEW) বেছে নাও এবং ঐচ্ছিকভাবে কারণ ব্যাখ্যা করো। স্বাক্ষরকারী সবসময় তুমিই, যে লগ-ইন করা ইউজার — এটা তোমার session থেকে নেওয়া হয় এবং টাইপ করে বদলানো যায় না, তাই কারো নামে ভুয়া স্বাক্ষর রেকর্ড করা সম্ভব না।',
    hi: 'किसी भी रिकॉर्ड के लिए एक CGMP-शैली का electronic signature दर्ज करें: Record Type चुनें और उसकी Record ID दर्ज करें, Action (APPROVE / REJECT / RELEASE / CLOSE / REVIEW) चुनें और वैकल्पिक रूप से कारण बताएं। हस्ताक्षरकर्ता हमेशा आप ही होते हैं, यानी लॉग-इन किया हुआ यूज़र — यह आपके session से लिया जाता है और टाइप करके बदला नहीं जा सकता, इसलिए किसी और के नाम पर हस्ताक्षर दर्ज नहीं किया जा सकता।',
    es: 'Captura una firma electrónica de estilo CGMP para cualquier registro: elige el Tipo de Registro e introduce su ID de Registro, elige la Acción (APPROVE / REJECT / RELEASE / CLOSE / REVIEW) y, opcionalmente, explica el motivo. El firmante siempre eres tú, el usuario con la sesión iniciada; se toma de tu sesión y no se puede escribir manualmente, por lo que una firma nunca puede registrarse a nombre de otra persona.',
    fr: "Enregistrez une signature électronique de style CGMP pour n'importe quel enregistrement : choisissez le Type d'Enregistrement et saisissez son ID, choisissez l'Action (APPROVE / REJECT / RELEASE / CLOSE / REVIEW) et, si vous le souhaitez, expliquez le motif. Le signataire est toujours vous, l'utilisateur connecté ; il est déterminé par votre session et ne peut pas être saisi manuellement, si bien qu'une signature ne peut jamais être enregistrée au nom de quelqu'un d'autre.",
    zh: '为任意记录采集一次CGMP风格的电子签名：选择记录类型并输入其记录ID，选择操作（Action：APPROVE / REJECT / RELEASE / CLOSE / REVIEW），并可选择说明签署理由。签署人始终是当前登录用户本人——该信息取自你的登录会话，无法手动输入更改，因此签名永远无法以他人名义记录。',
  },
};

// Only used if a topic key ever doesn't match one of the 9 tabs above.
const FALLBACK: HelpText = {
  en: 'Fill in the required fields (marked *) and press the primary button to save. Existing records appear in the table below the form.',
  bn: 'প্রয়োজনীয় ফিল্ড (* চিহ্নিত) পূরণ করে মূল বাটনে চাপ দিলে সেভ হয়ে যাবে। ফর্মের নিচের টেবিলে আগের রেকর্ডগুলো দেখা যাবে।',
  hi: 'आवश्यक फ़ील्ड (* चिह्नित) भरें और सेव करने के लिए मुख्य बटन दबाएं। मौजूदा रिकॉर्ड फॉर्म के नीचे टेबल में दिखाई देंगे।',
  es: 'Completa los campos obligatorios (marcados con *) y pulsa el botón principal para guardar. Los registros existentes aparecen en la tabla debajo del formulario.',
  fr: "Renseignez les champs obligatoires (marqués d'un *) et appuyez sur le bouton principal pour enregistrer. Les enregistrements existants apparaissent dans le tableau sous le formulaire.",
  zh: '填写必填字段（标有 * 号），然后点击主按钮保存。已有记录会显示在表单下方的表格中。',
};

export default function HelpButton({ topic }: { topic: string }) {
  const [open, setOpen] = useState(false);
  const [lang, setLang] = useState<LangCode>('en');
  if (!open) {
    return <button type="button" className="chip-btn help" onClick={() => setOpen(true)}><IconHelp size={15} /> Help</button>;
  }
  const text = HELP[topic]?.[lang] ?? HELP[topic]?.en ?? FALLBACK[lang] ?? FALLBACK.en;
  return (
    <div className="help-popover" onClick={() => setOpen(false)}>
      <div className="help-popover__card" onClick={e => e.stopPropagation()}>
        <button className="help-popover__close" onClick={() => setOpen(false)} aria-label="Close">×</button>
        <h3><IconHelp size={16} /> {topic} Help</h3>
        <div className="lang-tabs">
          {LANGS.map(l => (
            <button key={l.code} className={lang === l.code ? 'active' : ''} onClick={() => setLang(l.code)}>
              <IconGlobe size={11} /> {l.label}
            </button>
          ))}
        </div>
        <p>{text}</p>
      </div>
    </div>
  );
}
