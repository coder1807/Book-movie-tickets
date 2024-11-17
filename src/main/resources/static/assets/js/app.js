        function rate(value) {
            var stars = document.querySelectorAll('.rating-stars i');
            for (var i = 0; i < stars.length; i++) {
                if (i < value) {
                    stars[i].classList.add('selected');
                } else {
                    stars[i].classList.remove('selected');
                }
            }
            document.getElementById('rating').value = value;
        }

        function validateForm() {
            var rating = document.getElementById('rating').value;
            var content = document.getElementsByName('content')[0].value;

            if (!rating || rating == 0) {
                alert("Vui lòng chọn số sao để đánh giá.");
                return false;
            }

            if (!content.trim()) {
                alert("Vui lòng nhập nội dung đánh giá.");
                return false;
            }
            return true;
        }


        function handleCountryChange(selectElement) {
             const countryId = selectElement.value;
             if (countryId) {
                window.location.href = `/films/by-country?countryId=${countryId}`;
             }
        }
    function updatePaymentOption() {
            var selectedPayment = document.getElementById("payment").value;
            var paymentOptions = document.querySelectorAll('.payment-option li');
            paymentOptions.forEach(function(option) {
                var paymentValue = option.querySelector('.payment-value').innerText.toLowerCase();
                if (paymentValue === selectedPayment) {
                    option.classList.add('active');
                } else {
                    option.classList.remove('active');
                }
            });
    }
    function handlePromoForm(){
  event.preventDefault(); // Ngăn form submit lại trang

    var promoCode = document.getElementById("promoCode").value;
    var totalPrice = parseFloat(document.getElementById("totalPriceHidden").value);
    if (promoCode.toLowerCase() === "baanhem") {
        var discount = 0;
        if (totalPrice >= 200000) {
            discount = 0.2;
        } else if (totalPrice >= 100000) {
            discount = 0.15;
        } else if (totalPrice >= 80000) {
            discount = 0.1;
        } else {
            discount = 0.05;
        }
        var discountedPrice = totalPrice - (totalPrice * discount);
        document.getElementById("totalPriceDisplay").innerText = discountedPrice.toFixed(0) + " VND";
    } else {
        alert("Mã giảm giá không hợp lệ!");
    }
    }