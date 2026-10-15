'use client';

import React from 'react';
import { Topbar } from '@/components/organisms/Topbar';
import { Sidebar } from '@/components/organisms/Sidebar';
import { SidebarProvider } from '@/context/SidebarContext';

export interface DashboardLayoutProps {
  children: React.ReactNode;
  className?: string;
}

function DashboardContent({ children, className = '' }: DashboardLayoutProps) {
  return (
    <div className="min-h-screen bg-[#DDDDDD] p-2 md:p-6 flex justify-center items-start">
      <Sidebar />
      <div className="w-full max-w-[1880px] bg-white rounded-xl shadow-xl flex flex-col min-h-[calc(100vh-3rem)] overflow-hidden border border-[#CCCCCC]">
        <Topbar />
        <main className={`flex-1 p-6 md:p-10 bg-[#F6F6F6] ${className}`}>
          {children}
        </main>
      </div>
    </div>
  );
}

export const DashboardLayout: React.FC<DashboardLayoutProps> = (props) => {
  return (
    <SidebarProvider>
      <DashboardContent {...props} />
    </SidebarProvider>
  );
};

export default DashboardLayout;
