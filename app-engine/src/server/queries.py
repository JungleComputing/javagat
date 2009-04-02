import cgi

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db
from django.utils import simplejson 
from sets import Set 

class Bin(db.Model):
  path = db.StringProperty()
  val = db.StringProperty()
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
    bin1 = Bin()
    
    bin1.path = 'abc'
    bin1.val = 'key1'
    bin1.data = 'val1'
    bin1.put()
    
    bin = Bin(parent=bin1)
    bin.path = 'abc'
    bin.val = 'key2'
    bin.data = 'val2'
    bin.put()
    
    bin = Bin(parent=bin1)
    bin.path = 'abc'
    bin.val = 'key3'
    bin.data = 'val3'
    bin.put()
    
    bin = Bin(parent=bin1)
    bin.path = 'def'
    bin.val = 'key1'
    bin.data = 'val1'
    bin.put()
    
    bin = Bin()
    bin.path = 'ghi'
    bin.val = 'key2'
    bin.data = 'val2'
    bin.put()
    
    bin = Bin()
    bin.path = 'ghi'
    bin.val = 'key3'
    bin.data = 'val3'
    bin.put()
    
    bin = Bin()
    bin.path = 'ghi'
    bin.val = 'key4'
    bin.data = 'val4'
    bin.put()
    
    self.response.out.write('done.')

class TestQueries(webapp.RequestHandler):
  def get(self):
    query = db.GqlQuery("SELECT * FROM Bin")
    self.response.out.write("SELECT * FROM Bin<br />\n")
    
    fooDict = {}
    
    for bin in query:
       self.response.out.write("Key: %s<br />\n" % bin.val)
       self.response.out.write("Value: %s<br />\n" % bin.data)
       fooDict[bin.path] = bin.data
           
    self.response.out.write(simplejson.dumps(fooDict))
    
    self.response.out.write("**********<br />\n")

class TestFind(webapp.RequestHandler):
  def post(self):    
    body = self.request.body
    json = simplejson.loads(body)
    
    query = db.GqlQuery("SELECT * FROM Bin")
    
    paths = []
    
    for bin in query:
      paths.append(bin.path)
      
    paths  = list(Set(paths))
    self.response.out.write(paths)
    
    for path in paths[:]:
      for k in json.keys():
        query = db.GqlQuery("SELECT * FROM Bin WHERE path = :1 AND val = :2 AND data = :3", path, k, json[k])
        if query.count() < 1:
          paths.remove(path)
          break
    
    if len(paths) < 1:
      self.error(404)
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('Not Found')
      return
    
    self.response.out.write(simplejson.dumps(paths))    

application = webapp.WSGIApplication(
                                     [('/queries/', MainPage),
                                      ('/queries/find', TestFind),
                                      ('/queries/load', Load),
                                      ('/queries/test', TestQueries)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()