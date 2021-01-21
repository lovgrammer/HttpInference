from django.urls import path

from . import views

urlpatterns = [
    path('uploadimage/', views.uploadimage, name='uploadimage'),
]
