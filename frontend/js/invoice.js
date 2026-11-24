const PAINTBALL_JERSEY_PACKAGE_PRICE = 60;
const SIZES = ['xs', 's', 'm', 'l', 'xl', 'xl2', 'xl3', 'xl4', 'xl5', 'xl6', 'xl7'];
const SIZE_LABELS = ['XS', 'S', 'M', 'L', 'XL', '2XL', '3XL', '4XL', '5XL', '6XL', '7XL'];

function requiredElement(selector, root = document) {
  const element = root.querySelector(selector);
  if (!element) throw new Error(`The invoice form is missing ${selector}.`);
  return element;
}

function readNumber(input, description, integer = false) {
  const raw = input.value.trim();
  if (input.validity?.badInput) throw new Error(`Enter a valid number for ${description}.`);
  if (!raw) return 0;
  const value = Number(raw);
  if (!Number.isFinite(value) || value < 0 || (integer && !Number.isInteger(value))) {
    throw new Error(`Enter a valid non-negative ${integer ? 'whole number' : 'amount'} for ${description}.`);
  }
  return value;
}

function getSpecialRequests(container) {
  return Array.from(container.querySelectorAll('.special-item'), item => {
    const name = requiredElement('.special-name', item).value.trim();
    const priceInput = requiredElement('.special-price', item);
    const price = readNumber(priceInput, 'the special request price');
    const type = requiredElement('.special-type', item).value;
    if (!name && !priceInput.value.trim()) return null;
    if (!name || !priceInput.value.trim()) throw new Error('Enter both a name and a price for each special request.');
    return { name, price, type };
  }).filter(Boolean);
}

function extractAddOnCharge(value) {
  if (!value) return 0;
  const numericValue = Number(value);
  if (Number.isFinite(numericValue)) return numericValue;
  const match = value.match(/([+-])RM(\d+)/i);
  return match ? (match[1] === '-' ? -1 : 1) * Number(match[2]) : 0;
}

function getJobs() {
  return Array.from(document.querySelectorAll('.job-item'), card => {
    const jobType = card.dataset.jobType || 'standard';
    const jobName = requiredElement('.job-name', card).value.trim();
    if (!jobName) throw new Error('Enter a name for each job.');
    const sizingQuantity = Object.fromEntries(SIZES.map((size, index) => [size,
      readNumber(requiredElement(`.job-${size}`, card), `${SIZE_LABELS[index]} quantity`, true)
    ]));
    const quantity = Object.values(sizingQuantity).reduce((sum, value) => sum + value, 0);
    if (!quantity) throw new Error(`Enter at least one jersey for ${jobName}.`);
    const design = requiredElement('.job-design', card);
    const job = {
      jobType, jobName,
      basePrice: readNumber(requiredElement('.job-base-price', card), 'base price'),
      materialType: card.querySelector('.job-material')?.value || '',
      collarType: card.querySelector('.job-collar')?.value || '',
      sleeveType: card.querySelector('.job-sleeve')?.value || '',
      cuttingType: card.querySelector('.job-cutting')?.value || '',
      nameset: card.querySelector('.job-nameset')?.value || '',
      piping: card.querySelector('.job-piping')?.value || '',
      designType: design.value,
      designLabel: design.selectedOptions[0]?.textContent || '',
      sizingQuantity,
      specialRequests: getSpecialRequests(requiredElement('.job-special-container', card))
    };
    const paintball = jobType === 'paintball';
    const rate = job.basePrice + (paintball ? PAINTBALL_JERSEY_PACKAGE_PRICE : 0);
    const additions = {
      sleeve: extractAddOnCharge(job.sleeveType) * quantity,
      collar: extractAddOnCharge(job.collarType) * quantity,
      material: extractAddOnCharge(job.materialType) * quantity,
      cutting: extractAddOnCharge(job.cuttingType) * quantity,
      nameset: extractAddOnCharge(job.nameset) * quantity,
      piping: extractAddOnCharge(job.piping) * quantity,
      design: extractAddOnCharge(job.designType),
      plusSize: paintball ? 0 : (sizingQuantity.xl4 + sizingQuantity.xl5) * 5,
      bigSize: paintball ? 0 : (sizingQuantity.xl6 + sizingQuantity.xl7) * 8,
      specialRequests: job.specialRequests.reduce((sum, request) => sum + request.price * (request.type === 'pcs' ? quantity : 1), 0)
    };
    job.pricing = { quantity, rate, baseTotal: rate * quantity, ...additions };
    job.pricing.total = job.pricing.baseTotal + Object.values(additions).reduce((sum, value) => sum + value, 0);
    return job;
  });
}

export function buildOrderObject() {
  const value = id => requiredElement(`#${id}`).value.trim();
  const order = {
    orderDate: value('orderDate'), invoiceNumber: value('invoiceNumber'),
    companyName: value('companyName'), recipientName: value('name'),
    recipientEmail: value('email'), recipientNumber: value('phone'), postageAddress: value('address'),
    jobs: getJobs(), globalSpecialRequests: getSpecialRequests(requiredElement('#specialContainer')),
    postage: readNumber(requiredElement('#postage'), 'postage')
  };
  if (!order.orderDate || !order.invoiceNumber || !order.recipientName) {
    throw new Error('Enter the date, invoice number, and customer name.');
  }
  const email = requiredElement('#email');
  if (order.recipientEmail && !email.validity.valid) throw new Error('Enter a valid email address.');
  if (!order.jobs.length) throw new Error('Add at least one job.');
  const quantity = order.jobs.reduce((sum, job) => sum + job.pricing.quantity, 0);
  const jobsTotal = order.jobs.reduce((sum, job) => sum + job.pricing.total, 0);
  const globalSpecialTotal = order.globalSpecialRequests.reduce((sum, item) => sum + item.price * (item.type === 'pcs' ? quantity : 1), 0);
  order.pricing = { quantity, jobsTotal, globalSpecialTotal, postage: order.postage, total: jobsTotal + globalSpecialTotal + order.postage };
  return order;
}

export function handleInvoiceSubmission() {
  const status = document.getElementById('invoiceStatus');
  try {
    const order = buildOrderObject();
    generateInvoice(order);
    if (status) { status.textContent = 'Invoice generated. Your PDF download should begin shortly.'; status.dataset.state = 'success'; }
  } catch (error) {
    if (status) { status.textContent = error.message || 'Could not generate the invoice.'; status.dataset.state = 'error'; }
    else alert(error.message || 'Could not generate the invoice.');
  }
}

function generateInvoice(order) {
  if (!window.jspdf?.jsPDF) throw new Error('The PDF library could not be loaded. Check your internet connection and reload the page.');
  const pdf = new window.jspdf.jsPDF('p', 'mm', 'a4');
  if (typeof pdf.autoTable !== 'function') throw new Error('The PDF table library could not be loaded. Check your internet connection and reload the page.');
  const marginLeft = 14;
  pdf.setFont('helvetica', 'bold');
  pdf.setFontSize(11);
  pdf.text('JERSEY PRINTING SERVICE', marginLeft, 15);
  pdf.setFontSize(9);
  pdf.setFont('helvetica', 'normal');
  pdf.text('Mohamad Syazreeq Akmal', marginLeft, 20);
  pdf.text('akmalsyazreeq@gmail.com', marginLeft, 24);
  pdf.text('+60199483874', marginLeft, 28);
  pdf.setFontSize(22);
  pdf.setFont('helvetica', 'bold');
  pdf.text('INVOICE', 105, 45, { align: 'center' });
  pdf.setFontSize(10);
  pdf.setFont('helvetica', 'normal');
  pdf.text('Date:', 45, 60);
  pdf.line(60, 62, 100, 62);
  pdf.text(order.orderDate, 65, 60);
  pdf.text('No. Invoice:', 45, 70);
  pdf.line(75, 72, 130, 72);
  pdf.text(order.invoiceNumber.replace('#', ''), 80, 70);
  pdf.setFont('helvetica', 'bold');
  pdf.text('Bill to:', marginLeft, 90);
  pdf.setFont('helvetica', 'normal');
  pdf.text(order.companyName, marginLeft, 97);
  pdf.text(order.postageAddress, marginLeft, 102);
  pdf.text(`U.P.: ${order.recipientName}`, marginLeft, 112);
  pdf.text(order.recipientEmail, marginLeft, 117);
  pdf.text(order.recipientNumber, marginLeft, 122);

  const body = [];
  order.jobs.forEach(job => {
    const p = job.pricing;
    const sizes = SIZES.map((key, index) => [SIZE_LABELS[index], job.sizingQuantity[key]]).filter(([, quantity]) => quantity > 0);
    const description = job.jobType === 'paintball'
      ? `JOB: ${job.jobName}\nPACKAGE: Paintball Jersey Package\nDESIGN - ${job.designLabel}`
      : `JOB: ${job.jobName}\nMaterial: ${job.materialType}\nCollar: ${job.collarType}\nSleeve: ${job.sleeveType}\nDesign: ${job.designLabel}`;
    body.push([order.orderDate, description, sizes.map(([size, quantity]) => `${size}-${quantity}`).join('\n'), p.rate, p.quantity, p.baseTotal]);
    ['sleeve', 'collar', 'material', 'cutting', 'nameset', 'piping', 'design'].forEach(key => {
      const total = p[key];
      if (total !== 0) body.push([order.orderDate, `${key.toUpperCase()} (${job.jobName})${key === 'design' ? ` - ${job.designLabel}` : ''}`, '-', key === 'design' ? total : total / p.quantity, key === 'design' ? 1 : p.quantity, total]);
    });
    if (p.plusSize) body.push([order.orderDate, `PLUS SIZE 4XL-5XL (${job.jobName})`, job.sizingQuantity.xl4 + job.sizingQuantity.xl5, 5, job.sizingQuantity.xl4 + job.sizingQuantity.xl5, p.plusSize]);
    if (p.bigSize) body.push([order.orderDate, `PLUS SIZE 6XL-7XL (${job.jobName})`, job.sizingQuantity.xl6 + job.sizingQuantity.xl7, 8, job.sizingQuantity.xl6 + job.sizingQuantity.xl7, p.bigSize]);
    job.specialRequests.forEach(item => body.push([order.orderDate, `JOB: ${job.jobName} - ${item.name}`, '-', item.price, item.type === 'pcs' ? p.quantity : 1, item.price * (item.type === 'pcs' ? p.quantity : 1)]));
  });
  order.globalSpecialRequests.forEach(item => body.push([order.orderDate, `GLOBAL: ${item.name}`, '-', item.price, item.type === 'pcs' ? order.pricing.quantity : 1, item.price * (item.type === 'pcs' ? order.pricing.quantity : 1)]));
  if (order.postage > 0) body.push([order.orderDate, 'POSTAGE', '-', order.postage, 1, order.postage]);
  pdf.autoTable({ startY: 135, head: [['Date', 'Description', 'Size', 'Rate', 'Qty', 'Total']], body });
  let finalY = pdf.lastAutoTable.finalY + 10;
  if (finalY + 70 > pdf.internal.pageSize.getHeight() - 10) { pdf.addPage(); finalY = 20; }
  pdf.setFont('helvetica', 'bold');
  pdf.text(`Total: RM${order.pricing.total.toFixed(2)}`, 150, finalY);
  const disclaimerY = finalY + 20;
  pdf.setFontSize(8);
  pdf.setFont('helvetica', 'normal');
  pdf.text('This is a computer-generated invoice and does not require a signature or company stamp.', 105, disclaimerY, { align: 'center' });
  pdf.setFontSize(10);
  pdf.text('Payment Method: BANK TRANSFER', marginLeft, disclaimerY + 7);
  pdf.text('Bank Name: Maybank', marginLeft, disclaimerY + 12);
  pdf.text('Account Number: 162272989602', marginLeft, disclaimerY + 17);
  pdf.setFont('helvetica', 'bold');
  pdf.text('NOTES', marginLeft, disclaimerY + 24);
  pdf.setFont('helvetica', 'normal');
  pdf.text(['1. Please put INVOICE NUMBER on the reference', '   section upon payment.', '2. All defects, problems and etc should be informed to', '   us within 48 hours upon received. Any days from the', '   time given will not be accepted.'], marginLeft, disclaimerY + 29);
  pdf.save(`Invoice_${order.invoiceNumber.replace(/[\\/:*?"<>|]/g, '_')}.pdf`);
}
