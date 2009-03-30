import cgi

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson 

def auth(self):
    if not users.get_current_user():
        self.error(403)
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('Not Authenticated')
        return -1
    
    if not users.is_current_user_admin():
        self.error(403)
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('No Administrator')
        return -1
    
    return 0

class MainPage(webapp.RequestHandler):
  def get(self):
    print auth(self)

application = webapp.WSGIApplication(
                                     [('/func/', MainPage)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()