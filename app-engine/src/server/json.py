import cgi

from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson 

class MainPage(webapp.RequestHandler):
  def get(self):
    self.response.out.write("""
      <html>
      <body>
      This is a test page for using JSON.
      </body>
      </html>""")

class Download(webapp.RequestHandler):
  def post(self):
    data = self.request.body
    json = simplejson.loads(data)
    self.response.out.write(json[0] + '\n')
    for key in json[1].keys():
      self.response.out.write(key + " - " + json[1][key] + '\n')
    #self.response.out.write(json[2] + '\n')

class Display(webapp.RequestHandler):
  def get(self):
    self.response.out.write('TEST')
  
application = webapp.WSGIApplication(
                                     [('/json/', MainPage),
                                      ('/json/get', Download),
                                      ('/json/display', Display)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()