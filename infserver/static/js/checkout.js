$(function () {
    var IMP = window.IMP;
    IMP.init('imp43107622');
    $('.order-form').on('submit', function (e) {
        
        // var amount = parseFloat($('.order-form input[name="price"]').val().replace(',', ''));
        // var product_name = $('.order-form input[name="product_name"]').val()
        // var first_name = $('.order-form input[name="first_name"]').val()
        // var last_name = $('.order-form input[name="last_name"]').val()
        // var email = $('.order-form input[name="email"]').val()
        // var policy_confirmed = $('.order-form input[name="policy_confirmed"]').val()
        var paid_method = $("button").val(); 
        var order_id = AjaxStoreTransaction(e, paid_method)
        
        
        IMP.request_pay({
            pg : 'inicis',
            pay_method : 'card',
            merchant_uid: order_id,
            name: '[Relief] ' + product_name,
            buyer_name: last_name+first_name,
            buyer_email: email,
            amount: 100,
        }, function (rsp) {
            if (rsp.success) {
                var msg = '결제가 완료되었습니다.';
                msg += '고유ID : ' + rsp.imp_uid;
                msg += '상점 거래ID : ' + rsp.merchant_uid;
                msg += '결제 금액 : ' + rsp.paid_amount;
                msg += '카드 승인번호 : ' + rsp.apply_num;
                // 결제가 완료되었으면 비교해서 디비에 반영
                ImpTransaction(e, order_id, rsp.merchant_uid, rsp.imp_uid, rsp.paid_amount);
            } else {
                var msg = '결제에 실패하였습니다.';
                msg += '에러내용 : ' + rsp.error_msg;
                console.log(msg);
            }
        });
     
        return false;
    });
});

// iamport에 결제 정보가 있는지 확인 후 결제 완료 페이지로 이동
function ImpTransaction(e, order_id,merchant_id, imp_id, amount) {
    e.preventDefault();
    var request = $.ajax({
        method: "POST",
        url: order_validation_url,
        async: false,
        data: {
            order_id:order_id,
            merchant_id: merchant_id,
            imp_id: imp_id,
            amount: amount,
            csrfmiddlewaretoken: csrf_token
        }
    });
    request.done(function (data) {
        if (data.works) {
            $(location).attr('href', location.origin+order_complete_url+'?order_id='+order_id)
        }
    });
    request.fail(function (jqXHR, textStatus) {
        if (jqXHR.status == 404) {
            alert("페이지가 존재하지 않습니다.");
        } else if (jqXHR.status == 403) {
            alert("로그인 해주세요.");
        } else {
            console.log(jqXHR);
            alert("문제가 발생했습니다. 다시 시도해주세요.");
        }
    });
}

function AjaxStoreTransaction(e, paid_method) {
    e.preventDefault();
    var merchant_id = '';
    var request = $.ajax({
        method: "POST",
        url: order_checkout_url,
        async: false,
        data: {
            // cs_nickname: cs_nickname,
            amount: amount,
            option: option,
            policy_confirmed: policy_confirmed,
            paid_method: paid_method,
            csrfmiddlewaretoken: csrf_token
        }
    });
    request.done(function (data) {
        if (data.works) {
            merchant_id = data.merchant_id;
        }
    });
    request.fail(function (jqXHR, textStatus) {
        if (jqXHR.status == 404) {
            alert("페이지가 존재하지 않습니다.");
        } else if (jqXHR.status == 403) {
            alert("로그인 해주세요.");
        } else {
            alert("문제가 발생했습니다. 다시 시도해주세요.");
        }
    });
    return merchant_id;
}