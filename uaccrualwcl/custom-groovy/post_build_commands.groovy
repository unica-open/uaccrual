/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */


import it.csi.alm.Constants

def postBuildCommands(build_map) {
    sh """
        set -e

        echo "Workspace: \${WORKSPACE}"

        # Pulisci tutte le eventuali varianti di dist
        if ls "\${WORKSPACE}"/dist-arch* 1> /dev/null 2>&1; then
            echo "Rimuovo: \${WORKSPACE}/dist-arch*"
            rm -rf "\${WORKSPACE}"/dist-arch*
        fi

        echo "passo 2"
        mkdir -p "\${WORKSPACE}/dist-arch"

        # QUI: lascia che Groovy interpoli build_map.componentName
        #cd "\${WORKSPACE}/dist/${build_map.componentName}"

        zip -r "\${WORKSPACE}/dist-arch/dist_wcl.zip" ./
 echo "passo 3"
        # QUI: lascia che Groovy interpoli env.environment
        mkdir -p "\${WORKSPACE}/dist/${env.environment}"

        # Lowercase del nome componente (sh-compatibile)
        cmp_name_lowercase="\$(echo "${build_map.componentName}" | tr '[:upper:]' '[:lower:]')"
 echo "passo 4"
        # Estrazione versione usando grep -E (niente -P) e senza here-string
        cmp_version="\$(printf '%s' "${env.source_code}" | grep -Eo '[0-9]+\\.[0-9]+\\.[0-9]+' | head -n 1)"

        echo "cmp_name_lowercase=\${cmp_name_lowercase}"
        echo "cmp_version=\${cmp_version}"

        mv "\${WORKSPACE}/dist-arch/dist_wcl.zip" \
           "\${WORKSPACE}/dist/${env.environment}/\${cmp_name_lowercase}-\${cmp_version}.zip"
    """
}
return this

