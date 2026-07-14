/**
 * Aura Learning - Cloudflare Worker Book Sharing Website
 * Domain: https://aura.auralearning.workers.dev
 * 
 * Features:
 * - Dynamic Supabase connection to fetch real-time book listings.
 * - Single-page router that displays specific books when `?book={id_or_slug}` is specified.
 * - Gorgeous, responsive Material 3 styled UI with Tailwind CSS.
 * - Full catalog display of all books when no parameter is specified.
 * - Dynamic App Deep Link buttons that prompt users to open inside Aura Learning.
 * - Clean "Book Not Found" screen.
 */

const SUPABASE_URL = "https://qxoqflrqpwlythgqmjtq.supabase.co";
const SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF4b3FmbHJxcHdseXRoZ3FtanRxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIxODIxMTQsImV4cCI6MjA5Nzc1ODExNH0.cJ3hIsEyRtH1m_nmyzwjrdvzsbGIKIiChnmXAjgFRfo";

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    const bookParam = url.searchParams.get("book");

    // Fetch books from Supabase REST API
    let books = [];
    try {
      const response = await fetch(`${SUPABASE_URL}/rest/v1/books`, {
        headers: {
          "apikey": SUPABASE_ANON_KEY,
          "Authorization": `Bearer ${SUPABASE_ANON_KEY}`
        }
      });
      if (response.ok) {
        books = await response.json();
      }
    } catch (e) {
      console.error("Failed to fetch books from Supabase:", e);
    }

    // Helper functions for slug generation and matching
    function toSlug(str) {
      return str.toLowerCase()
        .replace(/[^a-z0-9\s-]/g, '')
        .replace(/\s+/g, '-')
        .replace(/-+/g, '-')
        .replace(/^-+|-+$/g, '');
    }

    function isBookMatch(book, query) {
      if (!book) return false;
      const q = query.toLowerCase().trim();
      if (book.id.toLowerCase() === q) return true;
      
      const nameSlug = toSlug(book.bookName);
      if (nameSlug === q) return true;
      
      const queryNormalized = q
        .replace("math-", "mathematics-")
        .replace("sst-", "social-studies-")
        .replace("computer-", "computer-science-");
        
      const nameSlugNormalized = nameSlug
        .replace("mathematics-", "math-")
        .replace("social-studies-", "sst-")
        .replace("computer-science-", "computer-");
        
      if (nameSlug === queryNormalized || nameSlugNormalized === q) return true;
      
      return false;
    }

    // Router Logic
    if (bookParam) {
      // Find the matched book
      const matchedBook = books.find(b => isBookMatch(b, bookParam));
      
      if (matchedBook) {
        return new Response(renderBookDetail(matchedBook, url.origin), {
          headers: { "Content-Type": "text/html; charset=utf-8" }
        });
      } else {
        return new Response(renderBookNotFound(bookParam, url.origin), {
          headers: { "Content-Type": "text/html; charset=utf-8" }
        });
      }
    }

    // Show Homepage
    return new Response(renderHomepage(books, url.origin), {
      headers: { "Content-Type": "text/html; charset=utf-8" }
    });
  }
};

// HTML Templates

function getHeader(title) {
  return `
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>${title}</title>
      <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
      <script src="https://cdn.tailwindcss.com"></script>
      <script>
        tailwind.config = {
          theme: {
            extend: {
              colors: {
                navy: {
                  primary: '#005AC1',
                  dark: '#002244',
                  slate: '#003F88',
                  light: '#E8F1FF'
                }
              },
              fontFamily: {
                sans: ['Inter', 'sans-serif'],
              }
            }
          }
        }
      </script>
      <style>
        body { font-family: 'Inter', sans-serif; }
      </style>
    </head>
    <body class="bg-[#F8FAFC] text-[#0F172A] min-h-screen flex flex-col">
  `;
}

function getFooter() {
  return `
      <footer class="bg-navy-dark text-white py-8 mt-auto border-t border-slate-800">
        <div class="max-w-6xl mx-auto px-6 text-center">
          <div class="flex items-center justify-center space-x-2 mb-4">
            <span class="text-2xl">📚</span>
            <span class="text-xl font-bold tracking-tight">Aura Learning</span>
          </div>
          <p class="text-sm text-slate-400">Your smart personalized digital classroom with AI doubt solvers and offline reading engines.</p>
          <div class="mt-4 flex justify-center space-x-6 text-sm text-slate-500">
            <a href="#" class="hover:text-white transition">Privacy Policy</a>
            <a href="#" class="hover:text-white transition">Terms of Service</a>
            <a href="#" class="hover:text-white transition">Contact Us</a>
          </div>
          <p class="text-xs text-slate-600 mt-6">&copy; ${new Date().getFullYear()} Aura EdTech. All rights reserved.</p>
        </div>
      </footer>
    </body>
    </html>
  `;
}

function renderBookDetail(book, originUrl) {
  const shareLink = `${originUrl}/?book=${encodeURIComponent(book.id)}`;
  // Safe default image
  const coverUrl = book.coverImage || "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=400&q=80";
  const appIntentLink = `intent://aura.auralearning.workers.dev/?book=${encodeURIComponent(book.id)}#Intent;scheme=https;package=con.aura.auralearning;end`;

  return `
    ${getHeader(book.bookName + " - Aura Learning")}
    
    <header class="bg-white border-b border-slate-200 sticky top-0 z-50">
      <div class="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <a href="/" class="flex items-center space-x-2">
          <span class="text-2xl">📚</span>
          <span class="text-lg font-bold text-navy-dark tracking-tight">Aura Learning</span>
        </a>
        <a href="/" class="text-sm font-semibold text-navy-primary hover:underline flex items-center space-x-1">
          <span>&larr;</span> <span>Back to Catalog</span>
        </a>
      </div>
    </header>

    <main class="max-w-4xl mx-auto px-6 py-12 flex-grow">
      <div class="bg-white rounded-3xl border border-slate-200 shadow-xl overflow-hidden md:flex">
        
        <!-- Book Cover Column -->
        <div class="bg-gradient-to-br from-navy-primary to-navy-dark p-8 md:w-2/5 flex flex-col justify-center items-center">
          <div class="relative group">
            <img 
              src="${coverUrl}" 
              alt="${book.bookName}" 
              class="w-48 h-72 object-cover rounded-xl shadow-2xl transition transform group-hover:scale-105 duration-300"
            />
            <div class="absolute -inset-1 rounded-xl bg-gradient-to-r from-yellow-400 to-amber-500 opacity-20 blur-lg group-hover:opacity-40 transition duration-300"></div>
          </div>
          <span class="text-xs font-bold tracking-widest text-[#E8F1FF]/80 mt-6 uppercase">OFFICIAL SYLLABUS</span>
        </div>

        <!-- Book Details Column -->
        <div class="p-8 md:w-3/5 flex flex-col justify-between">
          <div>
            <div class="flex flex-wrap gap-2 mb-4">
              <span class="bg-navy-light text-navy-primary text-xs font-semibold px-3 py-1 rounded-full uppercase tracking-wider">Class ${book.className}</span>
              <span class="bg-navy-light text-navy-primary text-xs font-semibold px-3 py-1 rounded-full uppercase tracking-wider">${book.subject}</span>
            </div>

            <h1 class="text-3xl font-extrabold text-navy-dark leading-snug mb-3">${book.bookName}</h1>
            <p class="text-sm font-medium text-slate-500 mb-6">Published by <span class="text-navy-primary font-semibold">Aura EdTech</span></p>
            
            <hr class="border-slate-100 my-4" />

            <h3 class="text-sm font-bold text-slate-800 uppercase tracking-wider mb-2">About this textbook</h3>
            <p class="text-slate-600 text-sm leading-relaxed mb-6">
              This high-quality, syllabus-aligned digital textbook is meticulously curated for Class ${book.className} students, focusing comprehensively on ${book.subject}. Open the book to study syllabus-aligned chapters, detailed explanations, diagrams, and integrated exercises.
            </p>
          </div>

          <!-- Actions -->
          <div class="space-y-3">
            <a 
              href="${book.pdfUrl}" 
              target="_blank" 
              class="w-full inline-flex justify-center items-center px-6 py-4 border border-transparent text-base font-bold rounded-2xl shadow-sm text-white bg-navy-primary hover:bg-navy-slate transition transform hover:-translate-y-0.5 duration-150"
            >
              📖 Read PDF Textbook Online
            </a>
            
            <a 
              href="${appIntentLink}" 
              class="w-full inline-flex justify-center items-center px-6 py-4 border-2 border-navy-primary text-base font-bold rounded-2xl text-navy-primary bg-navy-light hover:bg-navy-primary hover:text-white transition transform hover:-translate-y-0.5 duration-150"
            >
              ⚡ Open in Aura Learning App
            </a>
            
            <p class="text-center text-xs text-slate-400 mt-2">
              For the best personalized learning, interactive flashcards, progress trackers, and Gemini AI doubt solver, read this textbook inside our Android App.
            </p>
          </div>

        </div>
      </div>
    </main>

    ${getFooter()}
  `;
}

function renderHomepage(books, originUrl) {
  let bookCardsHtml = "";
  if (books.length === 0) {
    bookCardsHtml = `
      <div class="col-span-full text-center py-12">
        <span class="text-5xl">📚</span>
        <h3 class="text-lg font-bold text-slate-700 mt-4">No books available yet</h3>
        <p class="text-sm text-slate-500 mt-2">Check back soon for academic textbooks!</p>
      </div>
    `;
  } else {
    books.forEach(book => {
      const coverUrl = book.coverImage || "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80";
      const detailLink = `/?book=${encodeURIComponent(toSlug(book.bookName))}`;
      
      bookCardsHtml += `
        <div class="bg-white rounded-3xl border border-slate-200 overflow-hidden shadow-md hover:shadow-xl hover:-translate-y-1 transition duration-300 flex flex-col">
          <div class="h-48 bg-gradient-to-br from-navy-primary to-navy-dark relative flex items-center justify-center p-4">
            <img 
              src="${coverUrl}" 
              alt="${book.bookName}" 
              class="h-40 w-28 object-cover rounded shadow-lg transition duration-300"
            />
          </div>
          <div class="p-5 flex-grow flex flex-col justify-between">
            <div>
              <div class="flex items-center space-x-2 mb-2">
                <span class="bg-navy-light text-navy-primary text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider">Class ${book.className}</span>
                <span class="bg-slate-100 text-slate-600 text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider">${book.subject}</span>
              </div>
              <h3 class="font-extrabold text-navy-dark text-base leading-snug line-clamp-2 mb-2">${book.bookName}</h3>
            </div>
            <div class="mt-4 pt-4 border-t border-slate-100">
              <a href="${detailLink}" class="w-full text-center inline-flex justify-center items-center px-4 py-2 text-xs font-extrabold text-navy-primary hover:text-white border-2 border-navy-primary hover:bg-navy-primary rounded-xl transition duration-150">
                View & Share Book
              </a>
            </div>
          </div>
        </div>
      `;
    });
  }

  return `
    ${getHeader("Aura Learning - Digital Classroom & Book Sharing")}
    
    <!-- Hero Banner -->
    <div class="bg-gradient-to-br from-navy-dark via-navy-slate to-navy-primary text-white py-16 px-6 relative overflow-hidden">
      <div class="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,rgba(255,255,255,0.08),transparent)]"></div>
      <div class="max-w-5xl mx-auto text-center relative z-10">
        <span class="bg-[#E8F1FF]/20 text-white text-xs font-bold px-4 py-1.5 rounded-full uppercase tracking-widest mb-4 inline-block backdrop-blur-sm">AURA LEARNING COMPANION</span>
        <h1 class="text-4xl md:text-5xl font-extrabold tracking-tight mb-4 leading-tight">Your Digital Educational Sanctuary 📚</h1>
        <p class="text-lg md:text-xl text-[#E8F1FF]/90 font-light max-w-2xl mx-auto mb-8">
          Personalized curriculum textbooks, interactive flashcard decks, custom study planners, and on-demand Gemini AI tutor at your fingertips.
        </p>
        <div class="flex flex-col sm:flex-row justify-center items-center space-y-4 sm:space-y-0 sm:space-x-4">
          <a href="#" class="px-8 py-4 bg-white text-navy-dark font-extrabold rounded-2xl shadow-lg hover:shadow-2xl hover:scale-[1.02] transition duration-150">
            📲 Download Aura Learning App
          </a>
          <a href="#catalog" class="px-8 py-4 border-2 border-white/40 hover:border-white text-white font-extrabold rounded-2xl hover:bg-white/10 transition duration-150">
            📖 Browse Books Catalog
          </a>
        </div>
      </div>
    </div>

    <!-- Feature highlights -->
    <section class="py-12 bg-white border-b border-slate-200">
      <div class="max-w-5xl mx-auto px-6">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="flex items-start space-x-4">
            <div class="p-3 bg-navy-light text-navy-primary rounded-2xl text-2xl">🤖</div>
            <div>
              <h4 class="font-bold text-navy-dark">Gemini AI Tutor</h4>
              <p class="text-sm text-slate-500 mt-1">Stuck on questions? Ask Gemini AI directly from your book interface.</p>
            </div>
          </div>
          <div class="flex items-start space-x-4">
            <div class="p-3 bg-navy-light text-navy-primary rounded-2xl text-2xl">⚡</div>
            <div>
              <h4 class="font-bold text-navy-dark">Offline Reading Engine</h4>
              <p class="text-sm text-slate-500 mt-1">Download official school textbooks once and read them anywhere without internet.</p>
            </div>
          </div>
          <div class="flex items-start space-x-4">
            <div class="p-3 bg-navy-light text-navy-primary rounded-2xl text-2xl">⏰</div>
            <div>
              <h4 class="font-bold text-navy-dark">Interactive Planners</h4>
              <p class="text-sm text-slate-500 mt-1">Syllabus-aligned study boards, exam timers, and personal analytics to stay on track.</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Books Catalog -->
    <main id="catalog" class="max-w-6xl mx-auto px-6 py-16 flex-grow">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8">
        <div>
          <h2 class="text-3xl font-extrabold text-navy-dark tracking-tight">Official Textbook Library</h2>
          <p class="text-sm text-slate-500 mt-1">Official state and board textbooks curated for academic learning.</p>
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        ${bookCardsHtml}
      </div>
    </main>

    ${getFooter()}
  `;
}

function renderBookNotFound(query, originUrl) {
  return `
    ${getHeader("Book Not Found - Aura Learning")}
    
    <header class="bg-white border-b border-slate-200 sticky top-0 z-50">
      <div class="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <a href="/" class="flex items-center space-x-2">
          <span class="text-2xl">📚</span>
          <span class="text-lg font-bold text-navy-dark tracking-tight">Aura Learning</span>
        </a>
      </div>
    </header>

    <main class="max-w-md mx-auto px-6 py-16 flex-grow flex flex-col justify-center items-center text-center">
      <div class="p-6 bg-rose-50 text-rose-500 rounded-full text-5xl mb-6 shadow-md">🕵️‍♂️</div>
      <h1 class="text-2xl font-extrabold text-slate-800 mb-2">Book Not Found</h1>
      <p class="text-slate-500 text-sm mb-8 leading-relaxed">
        We couldn't find any textbook matching the query <span class="bg-slate-100 text-[#0F172A] font-mono px-1.5 py-0.5 rounded border border-slate-200">"${query}"</span>. Please verify your shared link or browse our entire official catalog.
      </p>
      
      <div class="w-full space-y-3">
        <a 
          href="/" 
          class="w-full inline-flex justify-center items-center px-6 py-4 text-base font-bold rounded-2xl shadow-md text-white bg-navy-primary hover:bg-navy-slate transition duration-150"
        >
          &larr; Back to Home Catalog
        </a>
      </div>
    </main>

    ${getFooter()}
  `;
}
