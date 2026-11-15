import { useState } from 'react'
import { Activity, BriefcaseBusiness, CalendarDays, ChevronDown, ChevronRight, CircleHelp, ClipboardList, Command, LayoutDashboard, Settings2, Users, WalletCards } from 'lucide-react'
import './App.css'
import Dashboard from './Dashboard'
import Requisitions from './Requisitions'
import Candidates from './Candidates'
import Applications from './Applications'
import Interviews from './Interviews'
import Offers from './Offers'

const navigation = [
  { label: 'Overview', icon: LayoutDashboard },
  { label: 'Requisitions', icon: BriefcaseBusiness },
  { label: 'Candidates', icon: Users },
  { label: 'Applications', icon: ClipboardList },
  { label: 'Interviews', icon: CalendarDays },
  { label: 'Offers', icon: WalletCards },
]

function App() {
  const [activePage, setActivePage] = useState('Overview')
  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><div className="brand-mark"><Command size={19} strokeWidth={2.4}/></div><div><span className="brand-name">RecruitOS</span><span className="brand-caption">TALENT OPERATIONS</span></div></div>
      <div className="workspace-switcher"><div className="workspace-avatar">Y</div><div className="workspace-copy"><strong>YottaFlex AI</strong><span>Workspace</span></div><ChevronDown size={15}/></div>
      <nav className="side-nav" aria-label="Main navigation"><span className="nav-heading">WORKSPACE</span>{navigation.map(({label,icon:Icon})=><button key={label} className={`nav-item ${activePage===label?'active':''}`} onClick={()=>setActivePage(label)}><Icon size={17} strokeWidth={1.9}/><span>{label}</span></button>)}</nav>
      <div className="sidebar-bottom"><div className="help-card"><div className="help-icon"><CircleHelp size={17}/></div><strong>Need a hand?</strong><p>Explore the workspace guide and workflow tips.</p><button onClick={()=>setActivePage('Help')}>View help <ChevronRight size={14}/></button></div><button className="nav-item" onClick={()=>setActivePage('Settings')}><Settings2 size={17}/><span>Settings</span></button><div className="user-profile"><div className="profile-avatar">PP</div><div className="profile-copy"><strong>Workspace Admin</strong><span>Administrator · Demo actor</span></div><ChevronDown size={15}/></div></div>
    </aside>
    <main className="main-panel">
      <header className="topbar"><div className="breadcrumbs"><span>Workspace</span><ChevronRight size={14}/><strong>{activePage}</strong></div><div className="topbar-actions"><span className="demo-indicator"><i/> API connected per module</span><div className="topbar-avatar">PP</div></div></header>
      {activePage==='Overview'?<Dashboard/>:activePage==='Requisitions'?<Requisitions/>:activePage==='Candidates'?<Candidates/>:activePage==='Applications'?<Applications/>:activePage==='Interviews'?<Interviews/>:activePage==='Offers'?<Offers/>:<div className="placeholder-page"><div className="placeholder-icon"><Activity size={24}/></div><span className="eyebrow">WORKSPACE MODULE</span><h1>{activePage}</h1><p>This module is outside the current recruitment workflow scope.</p><button className="button-secondary" onClick={()=>setActivePage('Overview')}>Back to overview</button></div>}
    </main>
  </div>
}
export default App
