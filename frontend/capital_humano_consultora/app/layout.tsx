import type { Metadata } from 'next';
import { Roboto } from 'next/font/google';
import './globals.css';
import { AuthProvider } from '@/context/AuthContext';

const roboto = Roboto({
  weight: ['300', '400', '500', '700'],
  subsets: ['latin'],
  variable: '--font-roboto',
  display: 'swap',
});

export const metadata: Metadata = {
  title: 'Consultora Capit@l Humano',
  description: 'Sistema Integral de Gestión de Puestos, Evaluaciones y Competencias',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="es" className={`${roboto.variable} h-full antialiased`}>
      <body className="min-h-full flex flex-col font-roboto bg-[#DDDDDD] text-[#000000]">
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
