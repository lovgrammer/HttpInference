from django.http import JsonResponse
from django.shortcuts import render, redirect, HttpResponse, reverse
from django.contrib.auth import login as auth_login
from django.contrib.auth import logout as auth_logout
from django.contrib.auth import authenticate
from django.contrib import messages
from django.views.generic import View, TemplateView, DetailView
from django.contrib.auth.views import PasswordResetView as AuthPasswordResetView, PasswordResetConfirmView as AuthPasswordResetConfirmView
from django.urls import reverse_lazy
from django.views.decorators.csrf import csrf_exempt

from django.contrib.auth.forms import (
    AuthenticationForm, PasswordChangeForm, PasswordResetForm, SetPasswordForm,
)
from .forms import UploadFileForm

from .inference import run_inference

class MainView(TemplateView):
    template_name = "segmentation/upload.html"
    
    @csrf_exempt
    def post(self, request, *args, **kwargs):
        form = UploadFileForm(request.POST, request.FILES)
        if form.is_valid():
            f = form.save()
            # return redirect('uploaded')
            run_inference('.' + f.file.url, 'media/output/out.png')
            return JsonResponse({'result': 200, 'file_name': '/media/output/out.png'}, status=200)
        
    def get(self, request, *args, **kwargs):
        form = UploadFileForm()
        ctx = {
            'form': form
        }
        return self.render_to_response(ctx)        

class UploadedView(TemplateView):
    template_name = "segmentation/uploaded.html"
    
    def get(self, request, *args, **kwargs):
        ctx = {}
        return self.render_to_response(ctx)
        
