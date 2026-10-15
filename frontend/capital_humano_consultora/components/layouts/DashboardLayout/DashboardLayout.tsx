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
    <div className="min-h-screen w-full flex flex-col bg-[#F6F6F6]">
      <Sidebar />
      <Topbar />
      <main className={`flex-1 w-full px-4 sm:px-8 lg:px-12 py-6 sm:py-8 ${className}`}>
        {children}
      </main>
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
