from django.shortcuts import render
from django.http import HttpResponse, JsonResponse

from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator

from .forms import ShareImageForm

@method_decorator(csrf_exempt)
def uploadimage(request):
    form = ShareImageForm(request.POST, request.FILES)

    if request.method == 'POST':
        print(request.FILES['image'])
        # if form.is_valid():
        #     handle_uploaded_file(request.FILES['image'])

    return JsonResponse({
        'result': 200
    })
