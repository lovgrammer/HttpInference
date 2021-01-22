from django.urls import path

from . import views

urlpatterns = [
    path('upload/', views.MainView.as_view(), name='main'),
    path('uploaded/', views.UploadedView.as_view(), name='uploaded'),    
]
