import { API_BASE_URL, createInvoice } from './api.js';

// =========================
// DROPDOWN CONFIG (FULL)
// =========================
const DROPDOWNS = {

  // ================= MATERIAL =================
  material: [
    // FREE
    "Eyelet 150GSM",
    "Spandex 260GSM",
    "RJ 170GSM",
    "Mini Eyelet 155GSM",
    "Honeycomb 150GSM",

    // +RM2
    "Diamond 155GSM (+RM2)",
    "Micro Mesh 160GSM (+RM2)",
    "Diamond Mesh 160GSM (+RM2)",
    "Lattice 145GSM (+RM2)",
    "UA Hexagon 165GSM (+RM2)",
    "Mini Mesh 160GSM (+RM2)",
    "UMC Jacquard 160GSM (+RM2)",

    // +RM4
    "Apple Mesh 155GSM (+RM4)",

    // +RM5
    "Pique 165GSM (+RM5)",

    // +RM8
    "Baseball 230GSM (+RM8)",

    // +RM10
    "Jacquard 9 150GSM (+RM10)",
    "Sandwich 250GSM (+RM10)",

    // +RM12
    "Computer Jacquard 170GSM (+RM12)",

    // +RM15
    "Lycra 280GSM (+RM15)"
  ],

  // ================= COLLAR =================
  collar: [
    // FREE
    "Roundneck",
    "V Neck Overlap",
    "V Neck Centre",

    // +RM2
    "Roundneck Overlap (+RM2)",
    "Roundneck Insert (+RM2)",
    "V Neck No End (+RM2)",
    "V Neck No End Overlap (+RM2)",
    "No End NBA (+RM2)",

    // +RM3
    "V Neck NFL (+RM3)",
    "Swoosh Classic (+RM3)",

    // +RM4
    "V Neck No End NFL (+RM4)",
    "No End Insert (+RM4)",
    "Open Stub (+RM4)",

    // +RM5
    "V Neck Stub (+RM5)",
    "Henley Button (+RM5)",
    "Polo Button (+RM5)",
    "Polo Boxflat (+RM5)",
    "Mandarin Button (+RM5)",
    "Mandarin Hidden Button (+RM5)",

    // +RM6
    "Polo Retro NFL (+RM6)",
    "Polo Retro (+RM6)",
    "Retro No End (+RM6)",
    "Retro Overlap (+RM6)",
    "Mandarin Zip (+RM6)",

    // +RM8
    "Polo Zip (+RM8)",
    "Retro V Line (+RM8)",
    "Retro Insert (+RM8)",
    "Retro No End NFL (+RM8)",
    "Retro Overlap Insert (+RM8)"
  ],

  // ================= SLEEVE =================
  sleeve: [
    "Short Sleeve",
    "Long Sleeve (+RM3)",
    "Sleeveless (-RM2)",
    "Extended Sleeve (+RM1)"
  ],

  // ================= NAMESET =================
  nameset: [
    "None",
    "Add Number (+RM3)",
    "Add Name (+RM3)",
    "Add Name+Number (+RM5)",
    "Add Name+Number (Front+Back) (+RM6)"
  ],

  // ================= CUTTING =================
  cutting: [
    "Standard",
    "Raglan",
    "NFL (+RM10)"
  ],

  // ================= PIPING =================
  piping: [
    "None",
    "Type A (+RM9)",
    "Type B (+RM9)",
    "Type C (+RM9)",
    "Type D (+RM5)"
  ]

};

const jobContainer = document.getElementById('jobContainer');
const addJobBtn = document.getElementById('addJob');


addJobBtn?.addEventListener('click', () => {
  if (!jobContainer || jobContainer.querySelector('.job-type-selector')) return;

  const selector = document.createElement('div');
  selector.classList.add('job-type-selector');
  selector.innerHTML = `
    <p>Select a job type</p>
    <div class="job-type-options">
      <button type="button" class="job-type-option" data-job-type="standard">Standard Job</button>
      <button type="button" class="job-type-option" data-job-type="paintball">Paintball Jersey Job</button>
    </div>
  `;

  jobContainer.appendChild(selector);

  selector.querySelectorAll('.job-type-option').forEach(button => {
    button.addEventListener('click', () => {
      createJobCard(button.dataset.jobType);
      selector.remove();
    });
  });
});

function createJobCard(jobType) {
  const job = document.createElement('div');
  job.classList.add('job-item');
  job.dataset.jobType = jobType;

  const isPaintballJob = jobType === 'paintball';

  job.innerHTML = isPaintballJob ? `
    <div class="section job-card paintball-job-card">
      <h3>Paintball Jersey Job</h3>

      <div class="field">
        <label>Job Name</label>
        <input type="text" aria-label="Job name" class="job-name" placeholder="e.g. Team A Jersey">
      </div>
      <br>
      <div class="field">
        <label>Base Price (per pcs)</label>
        <p>Calculated from the quantity. The paintball package adds RM60 per piece.</p>
      </div>

      <h3>Size Quantity</h3>
      ${createSizeQuantityFields()}

      <div class="field">
        <h3>Design</h3>
        ${createDesignField()}
      </div>

      <div class="field"><h3>Special Request (This Job)</h3><div class="job-special-container"></div><button type="button" class="add-job-special">+ Add Request</button></div>
      <button type="button" class="remove-job">Remove Job</button>
    </div>
  ` : `
    <div class="section job-card">

      <!-- JOB NAME -->
      <div class="field">
        <label>Job Name</label>
        <input type="text" aria-label="Job name" class="job-name" placeholder="e.g. Team A Jersey">
      </div>
      <br>
      <div class="field">
        <label>Base Price (per pcs)</label>
        <p>Calculated from the quantity.</p>
      </div>

      <!-- SIZE -->
      <h3>Size Quantity</h3>
      ${createSizeQuantityFields()}

      <!-- PRODUCT CONFIG -->
      <h3>Product</h3>
      <div class="grid-2">
        <select class="job-material" aria-label="Material">
            ${generateOptions(DROPDOWNS.material)}
        </select>
        <select class="job-collar" aria-label="Collar">
            ${generateOptions(DROPDOWNS.collar)}
        </select>
      </div>

    <!-- ADD ONS -->
    <h3>Add Ons</h3>

    <div class="grid-2">

    <div class="field">
        <label>Sleeve</label>
        <select class="job-sleeve" aria-label="sleeve">
        ${generateOptions(DROPDOWNS.sleeve)}
        </select>
    </div>

    <div class="field">
        <label>Nameset</label>
        <select class="job-nameset" aria-label="nameset">
        ${generateOptions(DROPDOWNS.nameset)}
        </select>
    </div>

    <div class="field">
        <label>Cutting</label>
        <select class="job-cutting" aria-label="cutting">
        ${generateOptions(DROPDOWNS.cutting)}
        </select>
    </div>

    <div class="field">
        <label>Piping</label>
        <select class="job-piping" aria-label="piping">
        ${generateOptions(DROPDOWNS.piping)}
        </select>
    </div>

    </div>
        <br>
      <!-- DESIGN -->
      <div class="field">
        <h3>Design</h3>
        ${createDesignField()}
      </div>
        <br>
      <!-- JOB SPECIAL REQUEST -->
      <div class="field">
        <h3>Special Request (This Job)</h3>
        <div class="job-special-container"></div>
        <button type="button" class="add-job-special">+ Add Request</button>
      </div>

      <button type="button" class="remove-job">Remove Job</button>

    </div>
  `;

  jobContainer.appendChild(job);

  // remove job
  job.querySelector('.remove-job').addEventListener('click', () => {
    job.remove();
  });

  // job special request logic
  const addJobSpecialButton = job.querySelector('.add-job-special');
  if (addJobSpecialButton) {
    addJobSpecialButton.addEventListener('click', () => {
    const container = job.querySelector('.job-special-container');

    const item = document.createElement('div');
    item.classList.add('special-item');

    item.innerHTML = `
      <div class="grid-3">
        <input type="text" class="special-name" aria-label="Request name" placeholder="Request Name">
        <input type="number" min="0" step="0.01" aria-label="Request price in RM" class="special-price" placeholder="Price">
        <select class="special-type" aria-label="Request charge type">
          <option value="pcs">Price / pcs</option>
          <option value="one">One-time</option>
        </select>
      </div>
      <button type="button" class="remove-btn">Remove</button>
    `;

    container.appendChild(item);

    item.querySelector('.remove-btn').addEventListener('click', () => {
      item.remove();
    });
    });
  }
}

function createSizeQuantityFields() {
  return `
    <div class="grid-5">
      <input type="number" min="0" step="1" class="job-xs" placeholder="XS" aria-label="XS quantity">
      <input type="number" min="0" step="1" class="job-s" placeholder="S" aria-label="S quantity">
      <input type="number" min="0" step="1" class="job-m" placeholder="M" aria-label="M quantity">
      <input type="number" min="0" step="1" class="job-l" placeholder="L" aria-label="L quantity">
      <input type="number" min="0" step="1" class="job-xl" placeholder="XL" aria-label="XL quantity">
      <input type="number" min="0" step="1" class="job-xl2" placeholder="2XL" aria-label="2XL quantity">
      <input type="number" min="0" step="1" class="job-xl3" placeholder="3XL" aria-label="3XL quantity">
      <input type="number" min="0" step="1" class="job-xl4" placeholder="4XL" aria-label="4XL quantity">
      <input type="number" min="0" step="1" class="job-xl5" placeholder="5XL" aria-label="5XL quantity">
      <input type="number" min="0" step="1" class="job-xl6" placeholder="6XL" aria-label="6XL quantity">
      <input type="number" min="0" step="1" class="job-xl7" placeholder="7XL" aria-label="7XL quantity">
    </div>
  `;
}

function createDesignField() {
  return `
    <select class="job-design" aria-label="Design">
      <option value="Customer Provided">Customer Provided</option>
      <option value="Company Template">Company Template</option>
      <option value="Custom Design (+RM50)">Custom Design (+RM50)</option>
    </select>
  `;
}

const addBtn = document.getElementById('addSpecial');
const container = document.getElementById('specialContainer');


addBtn?.addEventListener('click', () => {
  if (!container) return;

  const wrapper = document.createElement('div');
  wrapper.classList.add('special-item');

  wrapper.innerHTML = `
    <div class="grid-3">

      <div class="field">
        <label>Request Name</label>
        <input type="text" class="special-name" aria-label="Request name" placeholder="e.g. Extra Logo">
      </div>

      <div class="field">
        <label>Price (RM)</label>
        <input type="number" min="0" step="0.01" aria-label="Request price in RM" class="special-price" placeholder="e.g. 5">
      </div>

      <div class="field">
        <label>Charge Type</label>
        <select class="special-type" aria-label="Request charge type">
          <option value="pcs">Price / pcs</option>
          <option value="one">One-time</option>
        </select>
      </div>

    </div>

    <button type="button" class="remove-btn">Remove</button>
  `;

  container.appendChild(wrapper);

  // Remove button logic
  wrapper.querySelector('.remove-btn').addEventListener('click', () => {
    wrapper.remove();
  });
});

function readNumber(input, description, integer = false) {
  const raw = input.value.trim();
  if (input.validity.badInput) throw new Error(`Enter a valid number for ${description}.`);
  if (!raw) return 0;
  const value = Number(raw);
  if (!Number.isFinite(value) || value < 0 || (integer && !Number.isInteger(value))) {
    throw new Error(`Enter a valid non-negative ${integer ? 'whole number' : 'amount'} for ${description}.`);
  }
  return value;
}

function specialRequests(container) {
  return Array.from(container.querySelectorAll('.special-item'), item => {
    const name = item.querySelector('.special-name').value.trim();
    const input = item.querySelector('.special-price');
    const price = readNumber(input, 'the special request price');
    if (!name && !input.value.trim()) return null;
    if (!name || !input.value.trim()) throw new Error('Enter both a name and a price for each special request.');
    return { name, price, chargeType: item.querySelector('.special-type').value.toUpperCase() };
  }).filter(Boolean);
}

function buildInvoiceRequest() {
  const value = id => document.getElementById(id).value.trim();
  const sizeKeys = ['xs', 's', 'm', 'l', 'xl', 'xl2', 'xl3', 'xl4', 'xl5', 'xl6', 'xl7'];
  const jobs = Array.from(document.querySelectorAll('.job-item'), card => {
    const get = selector => card.querySelector(selector)?.value.trim() || null;
    const jobName = get('.job-name');
    if (!jobName) throw new Error('Enter a name for each job.');
    const sizes = Object.fromEntries(sizeKeys.map(size => [size, readNumber(card.querySelector(`.job-${size}`), `${size.toUpperCase()} quantity`, true)]));
    if (!Object.values(sizes).some(quantity => quantity > 0)) throw new Error(`Enter at least one jersey for ${jobName}.`);
    return {
      jobType: card.dataset.jobType.toUpperCase(), jobName, design: get('.job-design'), sizes,
      material: get('.job-material'), collar: get('.job-collar'), sleeve: get('.job-sleeve'),
      nameset: get('.job-nameset'), cutting: get('.job-cutting'), piping: get('.job-piping'),
      specialRequests: specialRequests(card.querySelector('.job-special-container'))
    };
  });
  if (!value('orderDate') || !value('name') || !value('phone') || !value('address')) {
    throw new Error('Enter the date, customer name, phone and address.');
  }
  if (value('email') && !document.getElementById('email').validity.valid) throw new Error('Enter a valid email address.');
  if (!jobs.length) throw new Error('Add at least one job.');
  const postage = document.getElementById('postage');
  return {
    date: value('orderDate'), postagePrice: postage.value.trim() ? readNumber(postage, 'postage') : null,
    customer: { name: value('name'), companyName: value('companyName') || null, email: value('email') || null,
      phone: value('phone'), address: value('address') },
    jobs, specialRequests: specialRequests(document.getElementById('specialContainer'))
  };
}

async function handleInvoiceSubmission() {
  const status = document.getElementById('invoiceStatus');
  const button = document.querySelector('.submit-btn');
  let preview;
  try {
    const invoice = buildInvoiceRequest();
    preview = window.open('', '_blank');
    button.disabled = true;
    status.textContent = 'Generating invoice...';
    status.dataset.state = '';
    const result = await createInvoice(invoice);
    const pdfUrl = new URL(result.pdfUrl, API_BASE_URL).href;
    document.getElementById('invoiceNumber').value = result.number;
    status.textContent = `Invoice ${result.number} generated. Total: RM${Number(result.pricing.total).toFixed(2)}. `;
    const link = document.createElement('a');
    link.href = pdfUrl;
    link.target = '_blank';
    link.rel = 'noopener';
    link.textContent = 'Open invoice PDF';
    status.append(link);
    status.dataset.state = 'success';
    if (preview) preview.location.href = pdfUrl;
  } catch (error) {
    if (preview) preview.close();
    status.textContent = error.message || 'Could not generate the invoice.';
    status.dataset.state = 'error';
  } finally {
    button.disabled = false;
  }
}

document.querySelector('.submit-btn')?.addEventListener('click', handleInvoiceSubmission);

function generateOptions(list) {
  return list.map(item => `<option value="${item}">${item}</option>`).join('');
}
