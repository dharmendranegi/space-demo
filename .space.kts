job("Deploy React App") {
    startOn {
        gitPush {
            branchFilter {
                +"refs/heads/master"
            }
        }
    }
    
    container("node:12") {
        shellScript {
            content = """
                npm cache clean --force
                npm install
                npm remove react-overlay-loading
                npm install react-scripts
                npm install react-overlay-loading
                npm run build
                mv artifact $mountDir/share
                ls -la $mountDir/share
            """
        }
    }
    
    container("microsoft/azure-cli") {
        env["AZURE_SUBSCRIPTION_ID"] = Secrets("subscription_id")
        env["AZURE_TENANT_ID"] = Secrets("tenant_id")
        env["AZURE_SERVICE_PRINCIPAL"] = Secrets("azure_service_principal")
        env["PASSWORD"] = Secrets("password")
        env["web"] = Secrets("web")
        env["ACCOUNT_KEY"] = Secrets("account_key")
        env["ACCOUNT_NAME"] = Params("account_name")
        env["RESOURCE_GROUP_NAME"] = Params("resource_group_name")

        shellScript {
            content = """
                echo SUBSCRIPTION Id for ${'$'}AZURE_SUBSCRIPTION_ID
                echo TENANT Id Secret for ${'$'}AZURE_TENANT_ID
                echo PASSWORD Secret for "${'$'}PASSWORD"
                echo AZURE_SERVICE_PRINCIPAL Secret for "${'$'}AZURE_SERVICE_PRINCIPAL"

                az login --service-principal -u ${'$'}AZURE_SERVICE_PRINCIPAL -p ${'$'}PASSWORD --tenant ${'$'}AZURE_TENANT_ID
                az storage blob sync --account-name ${'$'}ACCOUNT_NAME --account-key "${'$'}ACCOUNT_KEY" -c ${'$'}web -s "$mountDir/share/artifact"
            """
        }
    }
}