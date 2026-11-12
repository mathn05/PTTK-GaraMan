"use strict";

document.addEventListener("DOMContentLoaded", () => {
  const timer = document.querySelector("#holdTimer");
  if (timer) {
    // TTL đến từ DB; đo thời gian trôi qua để tránh lệch đồng hồ máy khách.
    const remaining = Math.max(0, Number(timer.dataset.remaining) || 0);
    const started = performance.now();
    const update = () => {
      const elapsedMillis = performance.now() - started;
      const remainingMillis = remaining - elapsedMillis;
      const seconds = Math.max(0, Math.ceil(remainingMillis / 1000));
      const minuteLabel = String(Math.floor(seconds / 60)).padStart(2, "0");
      const secondLabel = String(seconds % 60).padStart(2, "0");
      timer.querySelector("[data-countdown]").textContent = minuteLabel + ":" + secondLabel;
      if (!seconds) {
        timer.classList.add("expired");
        timer.querySelector("[data-countdown]").textContent = "Hết thời hạn giữ chỗ";
        document.querySelector("#confirmBooking").disabled = true;
      }
    };
    update();
    setInterval(update, 1000);
  }

  const booking = document.querySelector("#bookingForm");
  if (booking) {
    const day = booking.elements.appointmentDate;
    const button = booking.querySelector("[data-load-slots]");
    let version = 0;
    const loadSlots = async (event) => {
      event?.preventDefault();
      if (!day.value || !day.checkValidity()) {
        day.reportValidity();
        return;
      }
      const current = ++version;
      button.disabled = true;
      try {
        const url = new URL(booking.getAttribute("action"), document.baseURI);
        url.search = new URLSearchParams({
          action: "slots",
          context: booking.elements.context.value,
          appointmentDate: day.value
        });
        const response = await fetch(url, {headers: {"Accept": "text/html"}});
        if (!response.ok) {
          throw new Error("Không tải được khung giờ. Vui lòng thử lại.");
        }
        const html = await response.text();
        const doc = new DOMParser().parseFromString(html, "text/html");
        const slots = doc.querySelector(".slot-fieldset");
        if (!slots) {
          throw new Error("Phiên đã hết hạn. Vui lòng đăng nhập lại.");
        }

        // Chỉ thay vùng slot của phản hồi mới nhất; giữ xe và mô tả đang nhập.
        if (current === version) {
          booking.querySelector(".slot-fieldset").replaceWith(slots);
        }
      } catch (error) {
        if (current === version) {
          let message = document.querySelector("#slotError");
          if (!message) {
            message = document.createElement("p");
            message.id = "slotError";
            message.className = "alert";
            message.setAttribute("role", "alert");
            button.after(message);
          }
          message.textContent = error.message;
        }
      } finally {
        if (current === version) {
          button.disabled = false;
        }
      }
    };
    day.addEventListener("change", loadSlots);
    button.addEventListener("click", loadSlots);
  }
});
