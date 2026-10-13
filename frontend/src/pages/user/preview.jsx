import { BrowserRouter } from 'react-router-dom'
import { createRoot } from 'react-dom/client'
import '../../index.css'
import AuctionDetailPage from './AuctionDetailPage'

const auctionId = new URLSearchParams(window.location.search).get('auctionId') || '7'

createRoot(document.getElementById('root')).render(
  <BrowserRouter><AuctionDetailPage key={auctionId} auctionId={auctionId} preview /></BrowserRouter>,
)
