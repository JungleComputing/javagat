import cgi

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db

class Bin(db.Model):
  path = db.StringProperty()
  data = db.StringProperty()

class MainPage(webapp.RequestHandler):
  def get(self):
    self.response.out.write("""
      <html>
      <body>
      This is a test application for queries!<br />
      </body>
      </html>""")

class Load(webapp.RequestHandler):
  def get(self):
    bin = Bin()
    
    bin.path = 'abcdef'
    bin.data = 'none'
    bin.put()
    
    bin = Bin()
    bin.path = 'bcdefg'
    bin.data = 'none'
    bin.put()
    
    bin = Bin()
    bin.path = 'ghijkl'
    bin.data = 'none'
    bin.put()
    
    bin = Bin()
    bin.path = 'klmnop'
    bin.data = 'none'
    bin.put()
    
    self.response.out.write('done.')

class TestQueries(webapp.RequestHandler):
  def get(self):
    query = db.GqlQuery("SELECT * FROM Bin")
    self.response.out.write("SELECT * FROM Bin<br />\n")
    
    for bin in query:
       self.response.out.write("Key: %s<br />\n" % bin.key())
       self.response.out.write("Path: %s<br />\n" % bin.path)
    
    self.response.out.write("EOQ<br />\n")
    
    query = db.GqlQuery("SELECT * FROM Bin WHERE path = :1", "abcdef")
    self.response.out.write("SELECT * FROM Bin WHERE path = :1<br />\n")
    
    for bin in query:
       self.response.out.write("Key: %s<br />\n" % bin.key())
       self.response.out.write("Path: %s<br />\n" % bin.path)
    
    self.response.out.write("EOQ<br />\n")

    #query = db.GqlQuery("SELECT * FROM Bin WHERE path LIKE :1", "abcdef")
    self.response.out.write("SELECT * FROM Bin WHERE path LIKE :1<br />\n")
    
    #for bin in query:
    #   self.response.out.write("Key: %s<br />\n" % bin.key())
    #   self.response.out.write("Path: %s<br />\n" % bin.path)
    
    self.response.out.write("EOQ<br />\n")

application = webapp.WSGIApplication(
                                     [('/queries/', MainPage),
                                      ('/queries/load', Load),
                                      ('/queries/test', TestQueries)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()