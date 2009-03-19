import cgi

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app

class MainPage(webapp.RequestHandler):
  def get(self):
    self.response.out.write("""
      <html>
        <body>
          <form action="/forms/sign" method="post">
            <div><input type="text" name="author"></div>
            <div><textarea name="content" rows="3" cols="60"></textarea></div>
            <div><input type="submit" value="Sign Guestbook"></div>
          </form>
        </body>
      </html>""")

class Guestbook(webapp.RequestHandler):
  def post(self):
    self.response.out.write('<html><body><b>%s</b> wrote:<pre>' 
      % cgi.escape(self.request.get('author')))
    self.response.out.write(cgi.escape(self.request.get('content')))
    self.response.out.write('</pre></body></html>')

application = webapp.WSGIApplication(
                                     [('/forms/', MainPage),
                                      ('/forms/sign', Guestbook)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()