export interface Fund {
    id: number;
    nome: string;
    cnpj?: string | null;
    taxaBase: number;
    contaId?: number | null;
    saldo?: number | null;
    conta?: {
        id?: number;
        identificador?: string | null;
    } | null;
}

export interface Company {
    id: number;
    razaoSocial: string;
}

export interface ReceivableType {
    id: number;
    nome: string;
    spread?: number | null;
}

export interface Receivable {
    id: number;
    valorFace?: number | null;
    valorPresente?: number | null;
    dataVencimento?: string | null;
    fundoId?: number | null;
    tipoId?: number | null;
    empresaId?: number | null;
    prazo?: number | null;
    spread?: number | null;
    taxaBase: number;
    fundo?: Fund | null;
    empresa?: Company | null;
    tipo?: {
        id?: number;
        spread?: number | null;
    } | null;
}

export interface FundPayload {
    id?: number;
    nome: string;
    cnpj: string;
    taxaBase: number;
    contaId: number | null;
}

export interface ReceivablePayload {
    id?: number;
    valorFace: number;
    valorPresente?: number | null;
    dataVencimento: string;
    fundoId: number | null;
    tipoId: number;
    empresaId: number;
    prazo?: number | null;
    spread?: number | null;
    taxaBase: number;
}

export interface UserAccess {
    id: number;
    nome: string;
    email: string;
    roles: string[];
    scopes: string[];
    fundoIds: number[];
    directScopes: string[];
}

export interface Role {
    id: number;
    nome: string;
    scopes: string[];
}

export interface PermissionScope {
    id: number;
    codigo: string;
    descricao: string;
}

export interface Currency {
    id: number;
    codigo: string;
    nome: string;
}

export interface StatementTransaction {
    id: number;
    valor: number;
    realizadaEm: string;
    status: string;
    contaOrigem?: {
        id?: number;
        identificador?: string;
        moeda?: Currency;
    } | null;
    contaDestino?: {
        id?: number;
        identificador?: string;
        moeda?: Currency;
    } | null;
}

export interface StatementFilters {
    dataInicial?: string;
    dataFinal?: string;
    fundoId?: number;
    empresaId?: number;
    moedaId?: number;
}
