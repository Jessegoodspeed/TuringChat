from django.urls import path
from . import views

# Routing take url pattern and call a function from views
urlpatterns = [
	path('rooms', views.index, name='index'),
	path('create', views.create, name='create'),
]
