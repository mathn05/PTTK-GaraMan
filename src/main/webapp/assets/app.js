"use strict";
document.addEventListener("DOMContentLoaded", () => {
  const timer = document.querySelector("#holdTimer");
  if (timer) {
    const remaining = Math.max(0, Number(timer.dataset.remaining) || 0);
    const started = performance.now();
    const update = () => {
      const seconds = Math.max(0, Math.ceil((remaining - (performance.now() - started)) / 1000));
      timer.querySelector("[data-countdown]").textContent = `${String(Math.floor(seconds / 60)).padStart(2, "0")}:${String(seconds % 60).padStart(2, "0")}`;
      if (!seconds) {
        timer.classList.add("expired");
        timer.querySelector("[data-countdown]").textContent = "Hết thời hạn giữ chỗ";
        document.querySelector("#confirmBooking").disabled = true;
      }
    };
    update(); setInterval(update, 1000);
  }
  const booking = document.querySelector("#bookingForm");
  if (booking) {
    const day = booking.elements.appointmentDate;
    const button = booking.querySelector("[data-load-slots]");
    let version = 0;
    const loadSlots = async (event) => {
      event?.preventDefault();
      if (!day.value || !day.checkValidity()) { day.reportValidity(); return; }
      const current = ++version;
      button.disabled = true;
      try {
        const url = new URL(booking.getAttribute("action"), document.baseURI);
        url.search = new URLSearchParams({action: "slots", context: booking.elements.context.value, appointmentDate: day.value});
        const response = await fetch(url, {headers: {"Accept": "text/html"}});
        if (!response.ok) throw new Error("Không tải được khung giờ. Vui lòng thử lại.");
        const doc = new DOMParser().parseFromString(await response.text(), "text/html");
        const slots = doc.querySelector(".slot-fieldset");
        if (!slots) throw new Error("Phiên đã hết hạn. Vui lòng đăng nhập lại.");
        if (current === version) booking.querySelector(".slot-fieldset").replaceWith(slots);
      } catch (error) {
        if (current === version) {
          let msg = document.querySelector("#slotError");
          if (!msg) { msg = document.createElement("p"); msg.id = "slotError"; msg.className = "alert"; msg.setAttribute("role", "alert"); button.after(msg); }
          msg.textContent = error.message;
        }
      } finally { if (current === version) button.disabled = false; }
    };
    day.addEventListener("change", loadSlots);
    button.addEventListener("click", loadSlots);
  }
});
