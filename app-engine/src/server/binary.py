import cgi

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db

class Bin(db.Model):
  path = db.StringProperty()
  data = db.BlobProperty()

class MainPage(webapp.RequestHandler):
  def get(self):
    self.response.out.write("""
      <html>
      <body>
      This is a test form. Don't use unless using for test purposes (and you know what you're doing)!<br />
      <form action="multipart" method="post" enctype="multipart/form-data"> 
          <div><label>Message:</label></div>
          <div><textarea name="path" rows="3" cols="60"></textarea></div>
          <div><label>Avatar:</label></div>
          <div><input type="file" name="object"/></div>
          <div><input type="submit" value="Test"></div>
      </form>
      </body>
      </html>""")

class Download(webapp.RequestHandler):
  def post(self):
    bin = Bin()
    uploaded_file = self.request.body
    bin.data = db.Blob(uploaded_file)
    bin.path = 'none'
    bin.put()
    self.redirect('/')

class MultiPart(webapp.RequestHandler):
  def post(self):
    bin = Bin()
    bin.path = self.request.get("path")
    uploaded_file = self.request.get("object")
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
  
class Modify(webapp.RequestHandler):
  def get(self):
    binlist = db.GqlQuery("SELECT * FROM Bin")
    
    for bin in binlist:
      bytes = bin.data
      break
    
    self.response.out.write(ord(bytes[0:1]))
    #self.response.headers['Content-Type'] = "image/gif"
    #self.response.out.write(bytes[10:])

class Listing(webapp.RequestHandler):
  def get(self):
    binlist = db.GqlQuery("SELECT * FROM Bin")
    
    for bin in binlist:
       self.response.out.write("Key: %s<br />\n" % bin.key())
       self.response.out.write("Path: %s<br />\n" % bin.path)
    
    self.response.out.write("EOF")

application = webapp.WSGIApplication(
                                     [('/binary/', MainPage),
                                      ('/binary/get', Download),
                                      ('/binary/multipart', MultiPart),
                                      ('/binary/display', Display),
                                      ('/binary/modify', Modify),
                                      ('/binary/list', Listing)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()