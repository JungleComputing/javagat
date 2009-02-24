import cgi

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db

class Bin(db.Model):
  data = db.BlobProperty()

class MainPage(webapp.RequestHandler):
  def get(self):
    self.response.out.write("""
      <html>
      <body>
      This is a test form. Don't use unless using for test purposes!<br />
      <form action="get" method="post">
          <div><label>Message:</label></div>
          <div><textarea name="content" rows="3" cols="60"></textarea></div>
          <div><input type="submit" value="Test"></div>
      </form>
      </body>
      </html>""")

class Download(webapp.RequestHandler):
  def post(self):
    bin = Bin()
    uploaded_file = self.request.body
    bin.data = db.Blob(uploaded_file)
    bin.put()
    self.redirect('/')
    
class Display(webapp.RequestHandler):
  def get(self):
    binlist = db.GqlQuery("SELECT * FROM Bin")
    
    for bin in binlist:
      self.response.headers['Content-Type'] = "image/gif"
      self.response.out.write(bin.data)
      break

class Listing(webapp.RequestHandler):
  def get(self):
    binlist = db.GqlQuery("SELECT * FROM Bin")
    
    for bin in binlist:
       self.response.out.write("Key: %s<br />\n" % bin.key())
    
    self.response.out.write("EOF")

application = webapp.WSGIApplication(
                                     [('/binary/', MainPage),
                                      ('/binary/get', Download),
                                      ('/binary/display', Display),
                                      ('/binary/list', Listing)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()